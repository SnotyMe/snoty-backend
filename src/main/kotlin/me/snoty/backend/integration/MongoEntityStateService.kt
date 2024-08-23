package me.snoty.backend.integration

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import me.snoty.backend.database.mongo.mongoCollectionPrefix
import me.snoty.backend.utils.listAsElements
import me.snoty.integration.common.diff.*
import me.snoty.integration.common.diff.state.EntityState
import me.snoty.integration.common.diff.state.EntityStateCollection
import me.snoty.integration.common.diff.state.NodeEntityStates
import me.snoty.integration.common.diff.state.updateStates
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.node.NodeDescriptor
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class MongoEntityStateService(
	mongoDB: MongoDatabase,
	integration: NodeDescriptor,
	meterRegistry: MeterRegistry,
	private val metricsPool: ScheduledExecutorService
) : EntityStateService {
	private val nodeEntityStates: EntityStateCollection = mongoDB.getCollection<NodeEntityStates>("${integration.mongoCollectionPrefix}.entityStates")
	private val userEntityChanges: EntityChangesCollection = mongoDB.getCollection<UserEntityChanges>("${integration.mongoCollectionPrefix}.entityChanges")
	private val entityDiffMetrics = EntityDiffMetrics(meterRegistry, integration.type, nodeEntityStates)

	override fun scheduleMetricsTask() {
		metricsPool.scheduleAtFixedRate(entityDiffMetrics.Job(), 0, 30, TimeUnit.SECONDS)
	}

	override suspend fun updateStates(
		node: Node,
		entities: List<IUpdatableEntity<out Any>>
	) {
		if (entities.isEmpty()) return
		val states = nodeEntityStates.find<NodeEntityStates>(Filters.eq(node._id))
			.firstOrNull()
			?: NodeEntityStates(node._id, setOf())
		val existingEntities: Set<EntityState> = states.entities
		val allChanges = entities.associateWith { entity ->
			val existing = existingEntities.find { it.id == entity.id.toString() }

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
						nodeId = node._id,
						entityType = it.key.type,
						entityId = it.key.id.toString(),
					)
					UserEntityChanges(descriptor, it.value)
				}
			)
		}

		nodeEntityStates.updateStates(node, entities)
	}

	override fun getStates(node: Node): Flow<EntityState>
		= nodeEntityStates.find<NodeEntityStates>(
				Filters.eq(node._id)
			)
			.listAsElements {
				it.entities
			}
}
