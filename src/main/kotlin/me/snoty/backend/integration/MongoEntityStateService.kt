package me.snoty.backend.integration

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import me.snoty.backend.database.mongo.Aggregations
import me.snoty.backend.database.mongo.aggregate
import me.snoty.integration.common.InstanceId
import me.snoty.integration.common.IntegrationDescriptor
import me.snoty.integration.common.diff.*
import me.snoty.integration.common.diff.state.EntityState
import me.snoty.integration.common.diff.state.EntityStateCollection
import me.snoty.integration.common.diff.state.UserEntityStates
import me.snoty.integration.common.diff.state.updateStates
import java.util.*
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MongoEntityStateService(
	mongoDB: MongoDatabase,
	integration: IntegrationDescriptor,
	meterRegistry: MeterRegistry,
	private val metricsPool: ScheduledExecutorService
) : EntityStateService {
	private val userEntityStates: EntityStateCollection = mongoDB.getCollection<UserEntityStates>("integration.${integration.name}.entityStates")
	private val userEntityChanges: EntityChangesCollection = mongoDB.getCollection<UserEntityChanges>("integration.${integration.name}.entityChanges")

	private val entityDiffMetrics = EntityDiffMetrics(meterRegistry, integration.name, userEntityStates)

	override fun scheduleMetricsTask() {
		metricsPool.scheduleAtFixedRate(entityDiffMetrics.Job(), 0, 30, TimeUnit.SECONDS)
	}

	override suspend fun updateStates(
		userID: UUID,
		instanceId: InstanceId,
		entities: List<IUpdatableEntity<out Any>>
	) {
		if (entities.isEmpty()) return

		val states = userEntityStates.find<UserEntityStates>(Filters.eq("_id", userID))
			.firstOrNull()
			?: UserEntityStates(userID, mapOf())
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
			userEntityChanges.insertMany(
				changes.map {
					val descriptor = EntityDescriptor(
						instanceId,
						it.key.type,
						it.key.id.toString(),
						userID
					)
					UserEntityChanges(descriptor, it.value)
				}
			)
		}

		userEntityStates.updateStates(userID, instanceId, entities)
	}

	override fun getEntities(userID: UUID, instanceId: InstanceId, type: String): Flow<EntityState> {
		val entity = "entity"
		return userEntityStates.aggregate<EntityState>(
			Aggregates.match(Filters.eq("_id", userID)),
			Aggregations.project(
				Projections.exclude("_id"),
				Projections.computed(entity, "\$${UserEntityStates::entities.name}.$instanceId")
			),
			Aggregates.unwind("\$$entity"),
			Aggregates.replaceRoot("\$$entity"),
			Aggregates.match(Filters.eq(EntityState::type.name, type))
		)
	}
}
