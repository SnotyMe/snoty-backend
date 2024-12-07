package me.snoty.backend.utils.http.tracing

import io.ktor.client.request.*
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor
import io.opentelemetry.instrumentation.ktor.client.AbstractKtorClientTracingBuilder

// io.opentelemetry.instrumentation.ktor.v3_0.InstrumentationProperties
internal const val INSTRUMENTATION_NAME = "io.opentelemetry.ktor-3.0"

class KtorClientTracingBuilder : AbstractKtorClientTracingBuilder(INSTRUMENTATION_NAME) {
	fun setSpanNameExtractor(spanNameExtractor: SpanNameExtractor<in HttpRequestData>) {
		clientBuilder.setSpanNameExtractor { spanNameExtractor }
	}

	internal fun build(): KtorClientTracing {
		return KtorClientTracing(
			instrumenter = clientBuilder.build(),
			propagators = getOpenTelemetry().propagators
		)
	}
}
