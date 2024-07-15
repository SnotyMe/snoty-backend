package me.snoty.backend.integration.flow

import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.snoty.backend.featureflags.FeatureFlags
import me.snoty.backend.observability.*
import me.snoty.backend.utils.flowWith
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.flow.FlowLogEntry
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.setAttribute
import org.slf4j.Logger

class FlowRunnerImpl(
	private val nodeRegistry: NodeRegistry,
	private val featureFlags: FeatureFlags,
	private val tracer: Tracer,
) : FlowRunner {
	lateinit var json: Json

	override fun execute(jobId: String, logger: Logger, node: RelationalFlowNode, input: IntermediateData): Flow<FlowLogEntry> {
		val rootSpan = tracer.spanBuilder("Flow starting with ${traceName(node)}")
			.setNodeAttributes(node = node, input = null, rootNode = true)
			.setAttribute(JOB_ID, jobId)
			.startSpan()

		if (featureFlags.get(featureFlags.flow_logFlow)) {
			logger.info("Starting flow {}", node)
		}

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
	private fun executeImpl(logger: Logger, span: Span, node: RelationalFlowNode, input: IntermediateData, depth: Int = 0): Flow<FlowLogEntry> {
		val processor = nodeRegistry.lookupHandler(node.descriptor)
			?: return flowOf(FlowLogEntry("No handler for descriptor ${node.descriptor}", node._id))
		// TODO: test with multiple inputs
		return flowWith(processor.nodeHandlerContext) {
			processor.process(logger, node, input)
		}
			.flatMapConcat { output ->
				node.next.asFlow()
					.flatMapConcat { nextNode ->
						logger.debug("Processing next {} with data {}", nextNode.descriptor, output)
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


	private fun SpanBuilder.setNodeAttributes(node: RelationalFlowNode, input: IntermediateData?, rootNode: Boolean = false) = apply {
		setAttribute("node.id", node._id.toString())
		setAttribute("node.descriptor", node.descriptor)
		setAttribute(USER_ID, node.userId)
		if (!rootNode) {
			if (featureFlags.get(featureFlags.flow_traceConfig)) {
				setAttribute("config", json.encodeToString(node.settings))
			}
			if (input != null && featureFlags.get(featureFlags.flow_traceInput)) {
				setAttribute("input", input.toString())
			}
		}
	}
}
