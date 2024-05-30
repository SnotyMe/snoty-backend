package me.snoty.integration.common.diff.state

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import me.snoty.integration.common.InstanceId
import me.snoty.integration.common.diff.IUpdatableEntity
import java.util.*

suspend fun EntityStateCollection.updateStates(userId: UUID, instanceId: InstanceId, entities: List<IUpdatableEntity<out Any>>) {
	val mapped = entities.mapTo(HashSet()) {
		EntityState(it.id.toString(), it.type, it.fields, it.checksum)
	}
	updateStates(userId, instanceId, mapped)
}

suspend fun EntityStateCollection.updateStates(userId: UUID, instanceId: InstanceId, entities: Set<EntityState>) {
	val update = Updates.set("entities.$instanceId", entities)
	val result = updateOne(Filters.eq("_id", userId), update)
	if (result.matchedCount == 0L) {
		insertOne(UserEntityStates(userId, mapOf(instanceId to entities)))
	}
}
