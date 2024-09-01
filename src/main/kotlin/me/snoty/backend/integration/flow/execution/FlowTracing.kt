package me.snoty.backend.integration.flow.execution

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.Tracer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.snoty.backend.featureflags.FeatureFlags
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

@Single
class FlowTracing(
	private val json: Json,
	private val featureFlags: FeatureFlags,
	openTelemetry: OpenTelemetry,
) : Tracer by openTelemetry.getTracer(FlowRunner::class) {
	fun createRootSpan(jobId: String, flow: Workflow): Span {
		val flowId = flow._id.toString()

		val rootSpan = spanBuilder("Flow $flowId")
			.setAttribute(JOB_ID, jobId)
			.setAttribute("flow.id", flowId)
			.startSpan()

		MDC.put("flow.id", flowId)
		MDC.put("user.id", flow.userId.toString())

		return rootSpan
	}

	fun SpanBuilder.setNodeAttributes(node: Node, input: IntermediateData?) = apply {
		setAttribute("node.id", node._id.toString())
		MDC.put("node.id", node._id.toString())
		setAttribute("node.descriptor", node.descriptor)
		setAttribute(USER_ID, node.userId)
		MDC.put("user.id", node.userId.toString())

		if (featureFlags.get(featureFlags.flow_traceConfig)) {
			setAttribute("config", json.encodeToString(node.settings))
		}
		if (input != null && featureFlags.get(featureFlags.flow_traceInput)) {
			setAttribute("input", input.toString())
		}
	}

	fun traceName(node: GenericNode) =
		"Node ${node.descriptor.id} (${node._id})"
}
