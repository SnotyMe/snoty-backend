package me.snoty.integration.common.diff.state

import com.mongodb.client.model.Filters
import me.snoty.backend.integration.config.flow.NodeId

suspend fun EntityStateCollection.updateStates(nodeId: NodeId, entities: Set<EntityState>) {
	val states = NodeEntityStates(nodeId, entities)
	val result = replaceOne(Filters.eq(nodeId), states)
	if (result.matchedCount == 0L) {
		insertOne(states)
	}
}
