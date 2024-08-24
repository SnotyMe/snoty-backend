package me.snoty.backend.test

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor

fun createOpenTelemetry(): OpenTelemetryWithExporters {
	val spanExporter = InMemorySpanExporter.create()
	val tracerProvider = SdkTracerProvider.builder()
		.addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
		.build()

	val openTelemetry = OpenTelemetrySdk.builder()
		.setTracerProvider(tracerProvider)
		.build()

	return OpenTelemetryWithExporters(
		openTelemetry = openTelemetry,
		spanExporter = spanExporter,
	)
}


data class OpenTelemetryWithExporters(
	val openTelemetry: OpenTelemetry,
	val spanExporter: InMemorySpanExporter,
)
