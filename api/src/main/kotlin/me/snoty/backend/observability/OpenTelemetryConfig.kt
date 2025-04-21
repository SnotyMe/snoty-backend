package me.snoty.backend.observability

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.TextMapPropagator

data class OtelConfig(
	val enabled: Boolean = false,
	val traces: OtelTracingConfig? = null,
	val logs: OtelLoggingConfig? = null,
	val propagators: List<ContextPropagator> = listOf(ContextPropagator.W3C_TRACE_CONTEXT, ContextPropagator.W3C_BAGGAGE),
)

enum class OtelTransport {
	GRPC,
	HTTP,
}

enum class OtelProcessor {
	BATCH,
	SIMPLE,
}

enum class ContextPropagator(val get: () -> TextMapPropagator) {
	W3C_TRACE_CONTEXT({ W3CTraceContextPropagator.getInstance() }),
	W3C_BAGGAGE({ W3CBaggagePropagator.getInstance() }),
}

data class OtelTracingConfig(
	val endpoint: String,
	val transport: OtelTransport,
	val processor: OtelProcessor = OtelProcessor.BATCH,
	val headers: Map<String, String> = emptyMap(),
)

data class OtelLoggingConfig(
	val endpoint: String,
	val transport: OtelTransport,
	val processor: OtelProcessor = OtelProcessor.BATCH,
	val headers: Map<String, String> = emptyMap(),
)
