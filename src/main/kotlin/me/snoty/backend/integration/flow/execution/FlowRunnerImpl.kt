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
import me.snoty.backend.integration.flow.NodeExecutionException
import me.snoty.backend.integration.flow.unwrap
import me.snoty.backend.integration.flow.unwrapNodeException
import me.snoty.backend.logging.KMDC
import me.snoty.backend.notifications.NotificationAttributes
import me.snoty.backend.notifications.NotificationService
import me.snoty.backend.observability.APPENDER_LOG_LEVEL
import me.snoty.backend.observability.setException
import me.snoty.backend.observability.subspan
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.wiring.flow.execution.FlowExecutionEvent
import me.snoty.backend.wiring.flow.execution.FlowExecutionEventService
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContextImpl
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.flow.WorkflowWithNodes
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.koin.core.annotation.Single
import org.slf4j.Logger
import org.slf4j.event.Level

private const val FLOW_FAILURE = "flow.failure"

@Single
class FlowRunnerImpl(
	private val nodeRegistry: NodeRegistry,
	private val intermediateDataMapperRegistry: IntermediateDataMapperRegistry,
	private val flowTracing: FlowTracing,
	private val flowExecutionService: FlowExecutionService,
	private val flowExecutionEventService: FlowExecutionEventService,
	private val notificationService: NotificationService,
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

		logger.trace("Processing flow {}", flow)

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

				val node = it?.unwrapNodeException()?.node
				val attributes =
					if (node == null) NotificationAttributes(FLOW_FAILURE, flowId = flow._id)
					else NotificationAttributes(FLOW_FAILURE, flowId = flow._id, nodeId = node._id)
				if (it == null) {
					notificationService.resolve(flow.userId.toString(), attributes)
				} else {
					val exception = it.unwrap()
					notificationService.send(
						userId = flow.userId.toString(),
						attributes = attributes,
						title = "Flow ${flow.name} failed",
						description = "Something went wrong during the execution of this Flow!\n$exception",
					)
				}
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
		if (node._id in visited) {
			val referencingNodes = visited
				.filter { nodeMap[it]?.next?.contains(node._id) == true }
			logger.error { "Cycle detected at node ${node.descriptor} (${node._id}, referenced by $referencingNodes)" }
			return emptyFlow()
		}

		val nodeLogName = "${node.descriptor.name} node \"${node.settings.name}\" (${node._id})"

		val handler = nodeRegistry.lookupHandler(node.descriptor)
			?: let {
				logger.error { "No handler found for $nodeLogName" }
				return emptyFlow()
			}
		
		val metadata = nodeRegistry.getMetadata(node.descriptor)

		fun Node.executeNextNodes(input: NodeOutput) = node.next
			.asFlow()
			.mapNotNull { nextNodeId ->
				nodeMap[nextNodeId] ?: let {
					logger.error { "Next node $nextNodeId not found" }
					null
				}
			}
			.flatMapConcat { nextNode ->
				val subspan = span.subspan(flowTracing, traceName(nextNode)) {
					setNodeAttributes(nextNode, input)
				}
				executeImpl(subspan, nextNode, input, visited + node._id, depth + 1)
			}

		if (metadata.position != NodePosition.START && input.isEmpty() && !metadata.receiveEmptyInput) {
			logger.debug { "Skipping $nodeLogName because it does not receive empty input." }
			return node.executeNextNodes(emptyList()).onCompletion { span.end() }
		}

		val context = NodeHandleContextImpl(
			intermediateDataMapperRegistry = intermediateDataMapperRegistry,
			logger = logger.underlyingLogger,
		)

		KMDC.put(APPENDER_LOG_LEVEL, (node.logLevel ?: logLevel).name)

		return flow {
			logger.debug { "Processing $nodeLogName with $input" }
			// pls fix Kotlin
			val data = with(context) { with(handler) { process(node, input) } }
			logger.debug { "Processed $nodeLogName" }
			if (metadata.position.logOutput && node.next.isEmpty()) {
				logger.debug { "Node \"${node.settings.name}\" (${node._id}) has no output nodes, would have emitted $data" }
			}

			emit(data)
		}
			.catch {
				// exception has already been handled
				// we rethrow to handle it again at the root
				if (it is NodeExecutionException) throw it
				span.setException(it)
				throw NodeExecutionException(node, it)
			}
			.flowOn(span.asContextElement() + MDCContext())
			.flatMapConcat { output ->
				node.executeNextNodes(output) // current Node's output is the next Node's input
			}
			.onCompletion {
				span.end()
			}
	}
}
