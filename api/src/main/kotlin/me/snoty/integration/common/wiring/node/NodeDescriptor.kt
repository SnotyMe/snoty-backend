package me.snoty.integration.common.wiring.node

import com.mongodb.client.model.Filters
import io.opentelemetry.api.trace.SpanBuilder
import kotlinx.serialization.Serializable
import org.bson.conversions.Bson
import org.koin.core.qualifier.named

@Serializable
data class NodeDescriptor(
	/**
	 * Namespace of the node. Usually the package name of the node handler.
	 */
	val namespace: String,
	val name: String
) {
	companion object {
		fun filter(namespace: String, name: String): Bson = Filters.and(
			Filters.eq(NodeDescriptor::namespace.name, namespace),
			Filters.eq(NodeDescriptor::name.name, name)
		)
	}

	val id: String
		get() = "$namespace:$name"
}

val NodeDescriptor.scope
	get() = named(id)

fun SpanBuilder.setAttribute(key: String, value: NodeDescriptor) {
	this.setAttribute("$key.namespace", value.namespace)
	this.setAttribute("$key.name", value.name)
}
