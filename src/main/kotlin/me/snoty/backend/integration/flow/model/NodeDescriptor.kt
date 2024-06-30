package me.snoty.backend.integration.flow.model

import com.mongodb.client.model.Filters
import org.bson.conversions.Bson

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
}

object Subsystem {
	const val INTEGRATION = "integration"
	const val PROCESSOR = "processor"
}
