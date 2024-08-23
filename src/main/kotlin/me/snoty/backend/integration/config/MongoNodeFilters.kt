package me.snoty.backend.integration.config

import com.mongodb.client.model.Filters
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.graph.GraphNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.bson.conversions.Bson
import java.util.*

internal fun buildUserIDFilter(userID: UUID?): Bson =
	when (userID) {
		null -> Filters.empty()
		else -> Filters.eq(GraphNode::userId.name, userID)
	}

/**
 * @return null if no nodes with this position
 */
internal fun buildPositionFilter(nodeRegistry: NodeRegistry, position: NodePosition?): Bson? =
	when (position) {
		null -> Filters.empty()
		else -> {
			val filters = nodeRegistry.lookupDescriptorsByPosition(position).map {
				val prefix = GraphNode::descriptor.name + "."
				Filters.and(
					Filters.eq(prefix + NodeDescriptor::type.name, it.type),
					Filters.eq(prefix + NodeDescriptor::subsystem.name, it.subsystem)
				)
			}

			when {
				// zero nodes with this position => zero results
				filters.isEmpty() -> null
				else -> Filters.or(filters)
			}
		}
	}
