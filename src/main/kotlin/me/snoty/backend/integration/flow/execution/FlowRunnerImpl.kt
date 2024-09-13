package me.snoty.backend.integration.flow.execution

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.internal.Slf4jLogger
import io.github.oshai.kotlinlogging.slf4j.logger
import io.opentelemetry.api.trace.Span
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.serialization.json.Json
import me.snoty.backend.featureflags.FeatureFlags
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.FlowExecutionException
import me.snoty.backend.integration.flow.logging.FlowLogService
import me.snoty.backend.observability.setException
import me.snoty.backend.observability.subspan
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.NodeHandleContextImpl
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.flow.WorkflowWithNodes
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.koin.core.annotation.Single
import org.slf4j.Logger

@Single
class FlowRunnerImpl(
	private val nodeRegistry: NodeRegistry,
	private val featureFlags: FeatureFlags,
	private val intermediateDataMapperRegistry: IntermediateDataMapperRegistry,
	private val flowTracing: FlowTracing,
	private val flowLogService: FlowLogService,
) : FlowRunner {
	lateinit var json: Json

	override suspend fun execute(
		jobId: String,
		logger: Logger,
		flow: WorkflowWithNodes,
		input: IntermediateData,
	) {
		val kLogger = KotlinLogging.logger(logger)
		val rootSpan = flowTracing.createRootSpan(jobId, flow)

		if (featureFlags.get(featureFlags.flow_logFlow)) {
			kLogger.info { "Starting flow ${flow._id}" }
		}

		@Suppress("UNCHECKED_CAST")
		val executionContext = FlowExecutionContext(
			nodeMap = flow.nodes.associateBy { it._id },
			logger = kLogger as Slf4jLogger<Logger>,
		)

		with(executionContext) {
			flow.nodes.filter {
				nodeRegistry.getMetadata(it.descriptor).position == NodePosition.START
			}
				.asFlow()
				.flatMapConcat {
					executeStartNode(rootSpan, it, input)
				}
				.onCompletion {
					rootSpan.end()
					flowLogService.setExecutionStatus(
						jobId,
						if (it == null) FlowExecutionStatus.SUCCESS else FlowExecutionStatus.FAILED,
					)
				}
				.collect()
		}
	}

	private fun FlowExecutionContext.executeStartNode(
		rootSpan: Span,
		node: FlowNode,
		input: IntermediateData
	) = with(flowTracing) {
		val subspan = rootSpan.subspan(traceName(node)) {
			setNodeAttributes(node, input)
		}
		executeImpl(subspan, node, input, emptyList())
			// propagate the span and MDC through the flow
			.flowOn(subspan.asContextElement() + MDCContext())
			.flowCatching(subspan)
			.catch { rawException ->
				logger.error(rawException) { "Exception during flow execution" }
				// set exception of root to explain the failure to the user
				val e = FlowExecutionException(rawException)
				rootSpan.setException(e)
				throw e
			}
			.onCompletion {
				subspan.end()
			}
	}

	/**
	 * Executes the flow for the given node with the given input.
	 * Will end the span when the flow completes.
	 */
	context(FlowTracing)
	private fun FlowExecutionContext.executeImpl(
		span: Span,
		node: FlowNode,
		input: IntermediateData,
		visited: List<NodeId>,
		depth: Int = 0,
	): Flow<Unit> {
		val handler = nodeRegistry.lookupHandler(node.descriptor)
			?: let {
				logger.error { "No handler found for node ${node.descriptor}" }
				return flowOf()
			}

		if (node._id in visited) {
			logger.error { "Cycle detected at node ${node.descriptor}" }
			return flowOf()
		}

		return flow {
			logger.debug { "Processing ${node.descriptor.id} node (${node._id}) with $input" }

			with(NodeHandleContextImpl(intermediateDataMapperRegistry = intermediateDataMapperRegistry, flowCollector = this)) {
				handler.process(logger.underlyingLogger, node, input)
			}
		}
			.flatMapConcat { output ->
				node.next.asFlow()
					.flatMapConcat { nextNodeId ->
						val nextNode = nodeMap[nextNodeId] ?: let {
							logger.error { "Next node $nextNodeId not found" }
							return@flatMapConcat flowOf()
						}

						val subspan = span.subspan(traceName(nextNode)) {
							setNodeAttributes(nextNode, output)
						}
						executeImpl(subspan, nextNode, output, visited + node._id, depth + 1)
					}
			}
			.flowOn(span.asContextElement() + MDCContext())
			.flowCatching(span)
			.onCompletion {
				span.end()
			}
	}
}
