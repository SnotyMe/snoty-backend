package me.snoty.backend.integration

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapMerge
import me.snoty.backend.database.mongo.aggregate
import me.snoty.backend.database.mongo.mongoCollectionPrefix
import me.snoty.backend.database.mongo.mongoField
import me.snoty.backend.database.mongo.upsertOne
import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.hooks.register
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.utils.bson.getIdAsString
import me.snoty.integration.common.diff.DiffResult
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.STATE_CODEC_REGISTRY
import me.snoty.integration.common.diff.checksum
import me.snoty.integration.common.diff.state.EntityState
import me.snoty.integration.common.diff.state.NodeEntityStates
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.flow.NodeDeletedHook
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class MongoEntityStateService(
	mongoDB: MongoDatabase,
	integration: NodeDescriptor,
	hookRegistry: HookRegistry,
	@Named(STATE_CODEC_REGISTRY) codecRegistry: CodecRegistry,
) : EntityStateService {
	private val nodeEntityStates = mongoDB.getCollection<NodeEntityStates>("${integration.mongoCollectionPrefix}:entityStates")
		.withCodecRegistry(codecRegistry)

	override suspend fun getLastState(nodeId: NodeId, entityId: String): EntityState? =
		nodeEntityStates.aggregate<EntityState>(
			Aggregates.match(Filters.eq(NodeEntityStates::nodeId.name, nodeId)),
			Aggregates.unwind(NodeEntityStates::entities.mongoField),
			Aggregates.match(Filters.eq("${NodeEntityStates::entities.name}.${EntityState::id.name}", entityId)),
			Aggregates.replaceRoot(NodeEntityStates::entities.mongoField)
		).firstOrNull()

	override fun getLastStates(nodeId: NodeId): Flow<EntityState> =
		nodeEntityStates.find(Filters.eq(NodeEntityStates::nodeId.name, nodeId))
			.flatMapMerge { it.entities.asFlow() }

	override suspend fun updateState(nodeId: NodeId, state: Document, diff: DiffResult) {
		val id = state.getIdAsString() ?: return
		suspend fun upsert() {
			val entityState = EntityState(id, state, state.checksum())

			nodeEntityStates.upsertOne(
				Filters.eq(NodeEntityStates::nodeId.name, nodeId),
				Updates.addToSet(NodeEntityStates::entities.name, entityState)
			)
		}

		suspend fun pull() {
			nodeEntityStates.updateOne(
				Filters.eq(NodeEntityStates::nodeId.name, nodeId),
				Updates.pull(NodeEntityStates::entities.name, Filters.eq(EntityState::id.name, id))
			)
		}

		when (diff) {
			DiffResult.Unchanged -> return
			is DiffResult.Deleted -> pull()
			is DiffResult.Created -> upsert()
			is DiffResult.Updated -> {
				pull()
				upsert()
			}
		}
	}

	override suspend fun updateStates(nodeId: NodeId, states: Collection<EntityStateService.EntityStateUpdate>) {
		nodeEntityStates.upsertOne(
			Filters.eq(NodeEntityStates::nodeId.name, nodeId),
			Updates.set(NodeEntityStates::entities.name, states.map { it.state })
		)
	}

	init {
		hookRegistry.register(NodeDeletedHook {
			delete(it)
		})
	}

	override suspend fun delete(node: Node) {
		nodeEntityStates.deleteOne(Filters.eq(NodeEntityStates::nodeId.name, node._id))
	}
}
