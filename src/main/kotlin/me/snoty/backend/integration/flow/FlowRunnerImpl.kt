package me.snoty.backend.integration.flow

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.flow.*
import me.snoty.backend.featureflags.FeatureFlags
import me.snoty.backend.observability.*
import me.snoty.integration.common.wiring.EdgeVertex
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.flow.FlowOutput
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.setAttribute
import org.slf4j.Logger

class FlowRunnerImpl(
	private val nodeRegistry: NodeRegistry,
	private val featureFlags: FeatureFlags,
	private val tracer: Tracer
) : FlowRunner {
	override fun execute(jobId: String, logger: Logger, node: RelationalFlowNode, input: EdgeVertex): Flow<FlowOutput> {
		val rootSpan = tracer.spanBuilder("Flow starting with ${traceName(node)}")
			.setNodeAttributes(node = node, input = null, rootNode = true)
			.setAttribute(JOB_ID, jobId)
			.startSpan()
		return with(tracer) {
			val subspan = rootSpan.subspan(traceName(node)) {
				setNodeAttributes(node, input)
			}
			executeImpl(logger, subspan, node, input)
				// propagate the span through the flow
				.flowOn(subspan.asContextElement())
				.flowCatching(subspan)
				.catch { rawException ->
					// set exception of root to explain the failure of the user
					val e = FlowExecutionException(rawException)
					rootSpan.setException(e)
					throw e
				}
				.onCompletion {
					rootSpan.end()
				}
		}
	}

	/**
	 * Executes the flow for the given node with the given input.
	 * Will end the span when the flow completes.
	 */
	context(Tracer)
	private fun executeImpl(logger: Logger, span: Span, node: RelationalFlowNode, input: EdgeVertex, depth: Int = 0): Flow<FlowOutput> {
		val processor = nodeRegistry.lookupHandler(node.descriptor)
			?: return flowOf(FlowOutput("No handler for descriptor ${node.descriptor}", node._id))

		return flow {
			logger.debug("Processing node {} with input {} at depth {}", node.descriptor, input, depth)
			emit(processor.process(node, input))
			logger.debug("Processed node {}", node.descriptor)
		}.flatMapConcat { output ->
			node.next.asFlow()
				.flatMapConcat { nextNode ->
					logger.debug("Processing next {} with output {}", nextNode.descriptor, output)
					val subspan = span.subspan(traceName(nextNode)) {
						setNodeAttributes(nextNode, output)
					}
					executeImpl(logger, subspan, nextNode, output, depth + 1)
						.flowOn(subspan.asContextElement())
				}
		}
			.flowOn(span.asContextElement())
			.flowCatching(span)
			.onCompletion {
				span.end()
			}
	}


	private fun SpanBuilder.setNodeAttributes(node: RelationalFlowNode, input: EdgeVertex?, rootNode: Boolean = false) = apply {
		setAttribute("node.id", node._id.toString())
		setAttribute("node.descriptor", node.descriptor)
		setAttribute(USER_ID, node.userId)
		if (!rootNode) {
			if (featureFlags.get(featureFlags.flow_traceConfig)) {
				setAttribute("config", node.config)
			}
			if (input != null && featureFlags.get(featureFlags.flow_traceEdgeVertex)) {
				setAttribute("input", input.toString())
			}
		}
	}
}
