package me.snoty.backend.integration.flow.execution

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.internal.Slf4jLogger
import io.github.oshai.kotlinlogging.slf4j.logger
import io.opentelemetry.api.trace.Span
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.serialization.json.Json
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.FlowExecutionException
import me.snoty.backend.integration.flow.logging.FlowLogService
import me.snoty.backend.observability.setException
import me.snoty.backend.observability.subspan
import me.snoty.backend.utils.flowWith
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.NodeHandleContextImpl
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.flow.WorkflowWithNodes
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.koin.core.annotation.Single
import org.slf4j.Logger

@Single
class FlowRunnerImpl(
	private val nodeRegistry: NodeRegistry,
	private val featureFlags: FlowFeatureFlags,
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

		if (featureFlags.logFlow) {
			kLogger.info { "Starting flow ${flow._id}" }
		}

		@Suppress("UNCHECKED_CAST")
		val executionContext = FlowExecutionContext(
			nodeMap = flow.nodes.associateBy { it._id },
			logger = kLogger as Slf4jLogger<Logger>,
		)

		flow.nodes
			.filter {
				nodeRegistry.getMetadata(it.descriptor).position == NodePosition.START
			}
			.asFlow()
			.flatMapMerge {
				executionContext.executeStartNode(rootSpan, it, listOf(input))
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

	private fun FlowExecutionContext.executeStartNode(
		rootSpan: Span,
		node: FlowNode,
		input: Collection<IntermediateData>
	) = with(flowTracing) {
		val subspan = rootSpan.subspan(traceName(node)) {
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
	context(FlowTracing)
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
			logger.error { "Cycle detected at node ${node.descriptor}" }
			return emptyFlow()
		}

		logger.debug { "Processing ${node.descriptor.id} node (${node._id}) with $input" }
		return flowWith<NodeHandleContext, NodeOutput>(NodeHandleContextImpl(intermediateDataMapperRegistry = intermediateDataMapperRegistry)) {
			emit(handler.process(logger.underlyingLogger, node, input))
		}
			.flowCatching(span)
			.flowOn(span.asContextElement() + MDCContext())
			.flatMapMerge { output ->
				node.next
					.asFlow()
					.mapNotNull { nextNodeId ->
						nodeMap[nextNodeId] ?: let {
							logger.error { "Next node $nextNodeId not found" }
							null
						}
					}
					.flatMapMerge { nextNode ->
						val subspan = span.subspan(traceName(nextNode)) {
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
