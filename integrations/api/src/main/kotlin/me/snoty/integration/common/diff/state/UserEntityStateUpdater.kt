package me.snoty.integration.common.diff.state

import com.mongodb.client.model.Filters
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.diff.IUpdatableEntity
import me.snoty.integration.common.wiring.Node
import java.util.*

suspend fun EntityStateCollection.updateStates(node: Node, entities: List<IUpdatableEntity<out Any>>) {
	val mapped = entities.mapTo(HashSet()) {
		EntityState(it.id.toString(), it.type, it.fields, it.checksum)
	}
	updateStates(node._id, mapped)
}

suspend fun EntityStateCollection.updateStates(nodeId: NodeId, entities: Set<EntityState>) {
	val states = NodeEntityStates(nodeId, entities)
	val result = replaceOne(Filters.eq(nodeId), states)
	if (result.matchedCount == 0L) {
		insertOne(states)
	}
}
