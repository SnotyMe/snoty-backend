package me.snoty.backend.integration.flow.execution

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.internal.Slf4jLogger
import io.github.oshai.kotlinlogging.slf4j.logger
import io.opentelemetry.api.trace.Span
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.slf4j.MDCContext
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.FlowExecutionException
import me.snoty.backend.logging.KMDC
import me.snoty.backend.observability.APPENDER_LOG_LEVEL
import me.snoty.backend.observability.setException
import me.snoty.backend.observability.subspan
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.wiring.flow.execution.FlowExecutionEvent
import me.snoty.backend.wiring.flow.execution.FlowExecutionEventService
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.NodeHandleContextImpl
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.flow.WorkflowWithNodes
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.koin.core.annotation.Single
import org.slf4j.Logger
import org.slf4j.event.Level

@Single
class FlowRunnerImpl(
	private val nodeRegistry: NodeRegistry,
	private val intermediateDataMapperRegistry: IntermediateDataMapperRegistry,
	private val flowTracing: FlowTracing,
	private val flowExecutionService: FlowExecutionService,
	private val flowExecutionEventService: FlowExecutionEventService,
) : FlowRunner {
	override suspend fun execute(
		jobId: String,
		triggeredBy: FlowTriggerReason,
		logger: Logger,
		logLevel: Level,
		flow: WorkflowWithNodes,
		input: NodeInput,
	) {
		val kLogger = KotlinLogging.logger(logger)
		val rootSpan = flowTracing.createRootSpan(jobId, flow)
		flowExecutionService.create(jobId, flow._id, triggeredBy)

		flowExecutionEventService.offer(FlowExecutionEvent.FlowStartedEvent(
			userId = flow.userId,
			flowId = flow._id,
			jobId = jobId,
			triggeredBy = triggeredBy,
		))
		kLogger.info { "Running ${flow.name} (${flow._id})" }

		@Suppress("UNCHECKED_CAST")
		val executionContext = FlowExecutionContext(
			nodeMap = flow.nodes.associateBy { it._id },
			logger = kLogger as Slf4jLogger<Logger>,
			logLevel = logLevel,
			flowTracing = flowTracing,
		)

		flow.nodes
			.asFlow()
			.filter {
				nodeRegistry.getMetadataOrNull(it.descriptor)?.position == NodePosition.START
			}
			.flatMapConcat {
				executionContext.executeStartNode(rootSpan, it, input)
			}
			.onCompletion {
				logger.info("Flow completed.")
				rootSpan.end()
				val status = if (it == null) FlowExecutionStatus.SUCCESS else FlowExecutionStatus.FAILED
				flowExecutionEventService.offer(FlowExecutionEvent.FlowEndedEvent(
					userId = flow.userId,
					flowId = flow._id,
					jobId = jobId,
					status = status,
				))
				flowExecutionService.setExecutionStatus(
					jobId,
					status,
				)
			}
			.collect()
	}

	private fun FlowExecutionContext.executeStartNode(
		rootSpan: Span,
		node: FlowNode,
		input: Collection<IntermediateData>
	) = with(flowTracing) {
		val subspan = rootSpan.subspan(flowTracing, traceName(node)) {
			setNodeAttributes(node, input)
		}

		executeImpl(subspan, node, input, visited = emptyList(), depth = 0)
			.flowOn(subspan.asContextElement() + MDCContext())
			.catch { nodeException ->
				logger.error(nodeException) { "Exception during flow execution" }
				val flowException = FlowExecutionException(nodeException)
				rootSpan.setException(flowException)
				throw flowException
			}
			.onCompletion {
				subspan.end()
			}
	}

	/**
	 * Executes the flow for the given node with the given input.
	 * Will end the span when the flow completes.
	 */
	private fun FlowExecutionContext.executeImpl(
		span: Span,
		node: FlowNode,
		input: Collection<IntermediateData>,
		visited: List<NodeId>,
		depth: Int,
	): Flow<Unit> {
		val handler = nodeRegistry.lookupHandler(node.descriptor)
			?: let {
				logger.error { "No handler found for node ${node.descriptor}" }
				return emptyFlow()
			}

		if (node._id in visited) {
			val referencingNodes = visited
				.filter { nodeMap[it]?.next?.contains(node._id) == true }
			logger.error { "Cycle detected at node ${node.descriptor} (${node._id}, referenced by $referencingNodes)" }
			return emptyFlow()
		}

		val context = NodeHandleContextImpl(
			intermediateDataMapperRegistry = intermediateDataMapperRegistry,
			logger = logger.underlyingLogger,
		)

		KMDC.put(APPENDER_LOG_LEVEL, (node.logLevel ?: logLevel).name)

		return flow {
			logger.debug { "Processing ${node.descriptor.name} node \"${node.settings.name}\" (${node._id}) with $input" }
			// pls fix Kotlin
			val data = with(context) { with (handler) { process(node, input) } }
			logger.debug { "Processed ${node.descriptor.name} node \"${node.settings.name}\" (${node._id})" }

			emit(data)
		}
			.flowCatching(span)
			.flowOn(span.asContextElement() + MDCContext())
			.flatMapConcat { output ->
				@Suppress("UnusedFlow") // yeah I don't think so
				if (output.isEmpty()) return@flatMapConcat emptyFlow()
				node.next
					.asFlow()
					.mapNotNull { nextNodeId ->
						nodeMap[nextNodeId] ?: let {
							logger.error { "Next node $nextNodeId not found" }
							null
						}
					}
					.flatMapConcat { nextNode ->
						val subspan = span.subspan(flowTracing, traceName(nextNode)) {
							setNodeAttributes(nextNode, output)
						}
						executeImpl(subspan, nextNode, output, visited + node._id, depth + 1)
					}
			}
			.onCompletion {
				span.end()
			}
	}
}
