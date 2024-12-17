package me.snoty.backend.observability

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.TracerProvider
import io.opentelemetry.context.Context
import java.util.UUID
import kotlin.reflect.KClass

fun OpenTelemetry.getTracer(clazz: KClass<*>): Tracer
	= getTracer(clazz.java.`package`.specificationTitle)

fun TracerProvider.getTracer(clazz: KClass<*>): Tracer
	= get(clazz.java.`package`.specificationTitle)

fun Span.subspan(tracer: Tracer, name: String, builder: SpanBuilder.() -> Unit): Span
	= tracer.spanBuilder(name)
		.also(builder)
		.apply {
			setParent(Context.current().with(this@subspan))
		}
		.startSpan()


fun SpanBuilder.setAttribute(key: AttributeKey<String>, value: UUID) = this.setAttribute(key, value.toString())

fun Span.setException(e: Throwable) {
	this.setStatus(StatusCode.ERROR)
	this.recordException(e)
}
