package me.snoty.backend.dev.monitoring

import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.ServiceAttributes
import me.snoty.backend.config.ConfigLoaderImpl
import me.snoty.backend.dev.spi.DevRunnable
import me.snoty.backend.observability.provideOpenTelemetryConfig

class OpenTelemetryDevSetup : DevRunnable() {
	override fun run() {
		if (provideOpenTelemetryConfig(ConfigLoaderImpl()).enabled) {
			logger.info { "OpenTelemetry is configured manually, skipping dev setup..." }
			return
		}

		val resource = Resource.getDefault().toBuilder().put(ServiceAttributes.SERVICE_NAME, "snoty-backend").build()

		val tracingEndpoint = System.getenv("OTEL_EXPORTER_OTLP_TRACING_ENDPOINT") ?: "http://localhost:4318/v1/traces"
		val sdkTracerProvider = SdkTracerProvider.builder()
			.addSpanProcessor(SimpleSpanProcessor.create(OtlpHttpSpanExporter.builder().setEndpoint(tracingEndpoint).build()))
			.setResource(resource)
			.build()

		val logsEndpoint = System.getenv("OTEL_EXPORTER_OTLP_LOGS_ENDPOINT") ?: "http://localhost:3100/otlp/v1/logs"
		val sdkLoggerProvider = SdkLoggerProvider.builder()
			// in dev, we use the SimpleLogRecordProcessor which immediately sends each log record
			// in production, use the BatchLogRecordProcessor!
			.addLogRecordProcessor(SimpleLogRecordProcessor.create(OtlpHttpLogRecordExporter.builder().setEndpoint(logsEndpoint).build()))
			.setResource(resource)
			.build()

		OpenTelemetrySdk.builder()
			.setTracerProvider(sdkTracerProvider)
			.setLoggerProvider(sdkLoggerProvider)
			.setPropagators(
				ContextPropagators.create(
					TextMapPropagator.composite(W3CTraceContextPropagator.getInstance(), W3CBaggagePropagator.getInstance())
				)
			)
			.buildAndRegisterGlobal()
	}
}
