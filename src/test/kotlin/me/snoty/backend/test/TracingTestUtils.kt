package me.snoty.backend.test

import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import me.snoty.backend.observability.getTracer
import kotlin.reflect.KClass

fun createTestTracer(clazz: KClass<*>): TracerAndExporter {
	val exporter = InMemorySpanExporter.create()
	val tracerProvider = SdkTracerProvider.builder()
		.addSpanProcessor(SimpleSpanProcessor.create(exporter))
		.build()
	tracerProvider.getTracer(clazz)
	return TracerAndExporter(tracerProvider.getTracer(clazz), exporter)
}

data class TracerAndExporter(val tracer: Tracer, val exporter: InMemorySpanExporter)
