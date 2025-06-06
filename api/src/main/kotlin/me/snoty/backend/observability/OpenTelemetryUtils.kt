package me.snoty.backend.observability

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

fun OpenTelemetry.getTracer(clazz: KClass<*>): Tracer
	= getTracer(clazz.java.`package`.specificationTitle)

fun Span.subspan(tracer: Tracer, name: String, builder: SpanBuilder.() -> Unit): Span
	= tracer.spanBuilder(name)
		.also(builder)
		.apply {
			setParent(Context.current().with(this@subspan))
		}
		.startSpan()


fun SpanBuilder.setAttribute(key: AttributeKey<String>, value: Uuid) = this.setAttribute(key, value.toString())

fun Span.setException(e: Throwable) {
	this.setStatus(StatusCode.ERROR)
	this.recordException(e)
}
