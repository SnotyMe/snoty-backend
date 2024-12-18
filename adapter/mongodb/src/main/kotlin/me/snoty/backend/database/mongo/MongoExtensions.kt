package me.snoty.backend.database.mongo

import io.opentelemetry.api.trace.SpanBuilder
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.bson.Document

val NodeDescriptor.mongoCollectionPrefix: String
	get() = "nodes:$namespace:$name"

fun SpanBuilder.setAttribute(key: String, value: Document) = this.setAttribute(key, value.toJson())
