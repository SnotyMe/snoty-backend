package me.snoty.integration.common.wiring.node

import com.mongodb.client.model.Filters
import io.opentelemetry.api.trace.SpanBuilder
import kotlinx.serialization.Serializable
import org.bson.conversions.Bson
import org.koin.core.qualifier.named

@Serializable
data class NodeDescriptor(
	/**
	 * Subsystem of the node, like `integration`, `processor`, possibly `extension`, etc
	 * @see [Subsystem]
	 */
	val subsystem: String,
	val type: String
) {
	companion object {
		fun filter(subsystem: String, type: String): Bson = Filters.and(
			Filters.eq(NodeDescriptor::subsystem.name, subsystem),
			Filters.eq(NodeDescriptor::type.name, type)
		)
	}

	val id: String
		get() = "$subsystem:$type"
}

val NodeDescriptor.scope
	get() = named(id)

fun SpanBuilder.setAttribute(key: String, value: NodeDescriptor) {
	this.setAttribute("$key.subsystem", value.subsystem)
	this.setAttribute("$key.type", value.type)
}

object Subsystem {
	const val INTEGRATION = "integration"
	const val PROCESSOR = "processor"
	const val FILTER = "filter"
}
