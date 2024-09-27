package me.snoty.backend.integration.flow.execution

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.Tracer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.snoty.backend.observability.JOB_ID
import me.snoty.backend.observability.USER_ID
import me.snoty.backend.observability.getTracer
import me.snoty.backend.observability.setAttribute
import me.snoty.integration.common.wiring.GenericNode
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.flow.Workflow
import me.snoty.integration.common.wiring.node.setAttribute
import org.koin.core.annotation.Single
import org.slf4j.MDC

interface FlowTracing : Tracer {
	fun createRootSpan(jobId: String, flow: Workflow): Span
	fun SpanBuilder.setNodeAttributes(node: Node, input: Collection<IntermediateData>?): SpanBuilder
	fun traceName(node: GenericNode): String
}

@Single
class FlowTracingImpl(
	private val json: Json,
	private val featureFlags: FlowFeatureFlags,
	openTelemetry: OpenTelemetry,
) : FlowTracing, Tracer by openTelemetry.getTracer(FlowRunner::class) {
	override fun createRootSpan(jobId: String, flow: Workflow): Span {
		val flowId = flow._id.toString()

		val rootSpan = spanBuilder("Flow $flowId")
			.setAttribute(JOB_ID, jobId)
			.setAttribute("flow.id", flowId)
			.startSpan()

		return rootSpan
	}

	override fun SpanBuilder.setNodeAttributes(node: Node, input: Collection<IntermediateData>?) = apply {
		setAttribute("node.id", node._id.toString())
		MDC.put("node.id", node._id.toString())
		setAttribute("node.descriptor", node.descriptor)
		setAttribute(USER_ID, node.userId)
		MDC.put("user.id", node.userId.toString())

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
