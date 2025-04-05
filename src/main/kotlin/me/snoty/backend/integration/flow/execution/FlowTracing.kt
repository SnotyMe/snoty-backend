package me.snoty.backend.integration.flow.execution

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.Tracer
import kotlinx.serialization.json.Json
import me.snoty.backend.logging.KMDC
import me.snoty.backend.observability.*
import me.snoty.backend.wiring.flow.FlowFeatureFlags
import me.snoty.integration.common.wiring.GenericNode
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.flow.Workflow
import me.snoty.integration.common.wiring.node.setAttribute
import org.koin.core.annotation.Single

interface FlowTracing : Tracer {
	fun createRootSpan(jobId: String, flow: Workflow): Span
	fun SpanBuilder.setNodeAttributes(node: Node, input: Collection<IntermediateData>?): SpanBuilder
	fun traceName(node: GenericNode): String
}

@Single(binds = [FlowTracing::class])
class FlowTracingImpl(
	private val json: Json,
	private val featureFlags: FlowFeatureFlags,
	openTelemetry: OpenTelemetry,
) : FlowTracing, Tracer by openTelemetry.getTracer(FlowRunner::class) {
	override fun createRootSpan(jobId: String, flow: Workflow): Span {
		val flowId = flow._id

		val rootSpan = spanBuilder("Flow $flowId")
			.setAttribute(JOB_ID, jobId)
			.setAttribute(FLOW_ID, flowId)
			.startSpan()

		return rootSpan
	}

	override fun SpanBuilder.setNodeAttributes(node: Node, input: Collection<IntermediateData>?) = apply {
		setAttribute(NODE_ID, node._id)
		KMDC.put(NODE_ID, node._id)
		setAttribute("node.descriptor", node.descriptor)
		setAttribute(USER_ID, node.userId)
		KMDC.put(USER_ID, node.userId.toString())

		if (featureFlags.traceConfig) {
			setAttribute("config", json.encodeToString(node.settings))
		}
		if (input != null && featureFlags.traceInput) {
			setAttribute("input", input.toString())
		}
	}

	override fun traceName(node: GenericNode) =
		"Node ${node.descriptor.id} (${node._id})"
}
