package me.snoty.core.node

import io.opentelemetry.api.trace.SpanBuilder
import kotlinx.serialization.Serializable
import org.koin.core.qualifier.named

@JvmInline
@Serializable
value class NodeType(val value: String)

val NodeType.scope
	get() = named(value)

fun SpanBuilder.setAttribute(key: String, value: NodeType) {
	this.setAttribute("$key.type", value.value)
}
