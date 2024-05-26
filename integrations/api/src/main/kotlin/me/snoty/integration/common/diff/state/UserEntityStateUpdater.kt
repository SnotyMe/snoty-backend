package me.snoty.integration.common.diff.state

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.flow.firstOrNull
import me.snoty.integration.common.InstanceId
import me.snoty.integration.common.diff.*
import java.util.*

suspend fun updateEntitiesInDB(
	entityDiffMetrics: EntityDiffMetrics,
	entityStateCollection: EntityStateCollection,
	entityChangeCollection: EntityChangeCollection,
	userId: UUID,
	instanceId: InstanceId,
	entities: List<IUpdatableEntity<out Any>>
) {
	if (entities.isEmpty()) return

	val states = entityStateCollection.find<UserEntityStates>(Filters.eq("_id", userId))
		.firstOrNull()
			?: UserEntityStates(userId, mapOf())
	val existingEntities: Set<EntityState>? = states.entities[instanceId]

	val allChanges = entities.associateWith { entity ->
		val existing = existingEntities?.find { it.id == entity.id.toString() }

		if (existing == null) {
			return@associateWith DiffResult.Created(entity.checksum, entity.fields)
		}

		return@associateWith entity.diff(existing.state)
	}
	entityDiffMetrics.process(allChanges.values)

	val changes = allChanges.filter { it.value !is DiffResult.Unchanged }

	if (changes.isNotEmpty()) {
		entityChangeCollection.insertMany(
			changes.map {
				val descriptor = Descriptor(
					instanceId,
					it.key.type,
					it.key.id.toString(),
					userId
				)
				UserEntityChanges(descriptor, it.value)
			}
		)
	}

	entityStateCollection.updateStates(userId, instanceId, entities)
}

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
