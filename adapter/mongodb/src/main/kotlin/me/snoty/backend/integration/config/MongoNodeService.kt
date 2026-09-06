package me.snoty.backend.integration.config

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.mongodb.kotlin.client.model.Filters
import com.mongodb.kotlin.client.model.Updates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.snoty.backend.database.mongo.EMPTY
import me.snoty.backend.database.mongo.deserializeOrInvalid
import me.snoty.backend.database.mongo.objectId
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.utils.bson.encode
import me.snoty.backend.wiring.node.MongoNode
import me.snoty.backend.wiring.node.NodeSettingsDeserializationService
import me.snoty.backend.wiring.node.toRelational
import me.snoty.backend.wiring.node.toStandalone
import me.snoty.core.flow.FlowId
import me.snoty.core.flow.Workflow
import me.snoty.core.node.FlowNode
import me.snoty.core.node.Node
import me.snoty.core.node.NodeId
import me.snoty.core.node.StandaloneNode
import me.snoty.core.user.UserId
import me.snoty.integration.common.config.NodePatch
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.config.NodeServiceResults
import me.snoty.integration.common.wiring.flow.NODE_COLLECTION_NAME
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.conversions.Bson
import org.koin.core.annotation.Single
import kotlin.time.Clock

@Single
class MongoNodeService(
	db: MongoDatabase,
	private val settingsDeserializationService: NodeSettingsDeserializationService,
) : NodeService {
	private val collection = db.getCollection<MongoNode>(NODE_COLLECTION_NAME)

	override suspend fun get(userId: UserId?, id: NodeId): StandaloneNode? {
		val mongoNode = collection.find(
			Filters.and(
				if (userId != null) Filters.eq(MongoNode::userId, userId) else Filters.EMPTY,
				Filters.eq(MongoNode::_id, id.objectId),
			)
		).firstOrNull() ?: return null

		val settings = settingsDeserializationService.deserializeOrInvalid(mongoNode)
		return mongoNode.toStandalone(settings)
	}

	override fun getByFlow(flowId: FlowId): Flow<FlowNode> = collection.find(
		Filters.eq(MongoNode::flowId, flowId.objectId)
	).map { node ->
		val settings = settingsDeserializationService.deserializeOrInvalid(node)
		node.toRelational(settings)
	}

	override suspend fun <S : NodeSettings> create(
		userId: UserId,
		flow: Workflow,
		descriptor: NodeDescriptor,
		name: String,
		position: NodePosition,
		settings: S,
	): StandaloneNode {
		val now = Clock.System.now()
		val node = MongoNode(
			flowId = flow.objectId,
			userId = userId,
			descriptor = descriptor,
			name = name,
			position = position,
			settings = collection.codecRegistry.encode(settings),
			next = emptyList(),
			createdAt = now,
			modifiedAt = now,
		)

		collection.insertOne(node)

		return node.toStandalone(settings)
	}

	override suspend fun connect(from: Node, to: Node): ServiceResult {
		collection.updateOne(
			Filters.eq(MongoNode::_id, from.objectId),
			Updates.combine(
				Updates.addToSet(MongoNode::next, to.objectId),
				Updates.set(MongoNode::modifiedAt, Clock.System.now())
			)
		)

		return NodeServiceResults.NodeConnected(from, to)
	}

	override suspend fun disconnect(from: Node, to: Node): ServiceResult {
		collection.updateOne(
			Filters.eq(MongoNode::_id, from.objectId),
			Updates.combine(
				Updates.pull(MongoNode::next, to.objectId),
				Updates.set(MongoNode::modifiedAt, Clock.System.now())
			)
		)

		return NodeServiceResults.NodeDisconnected(from, to)
	}

	override suspend fun updateSettings(node: Node, settings: NodeSettings) = updateNode(
		node,
		listOf(Updates.set(MongoNode::settings, collection.codecRegistry.encode(settings)))
	)

	override suspend fun patch(node: Node, patchRequest: NodePatch): ServiceResult = updateNode(
		node,
		listOfNotNull(
			patchRequest.name?.let { Updates.set(MongoNode::name, it) },
			patchRequest.position?.let { Updates.set(MongoNode::position, it) },
			patchRequest.logLevel?.let { logLevel ->
				when {
					logLevel.isPresent -> Updates.set(MongoNode::logLevel, logLevel.get())
					else -> Updates.unset(MongoNode::logLevel)
				}
			},
			patchRequest.settings?.let {
				Updates.set(MongoNode::settings, collection.codecRegistry.encode(it))
			}
		)
	)

	private suspend fun updateNode(node: Node, updates: Collection<Bson>): ServiceResult {
		val result = collection.updateOne(
			Filters.eq(MongoNode::_id, node.objectId),
			Updates.combine(*updates.toTypedArray(), Updates.set(MongoNode::modifiedAt, Clock.System.now()))
		)
		return when {
			result.matchedCount == 0L -> NodeServiceResults.NodeNotFoundError(node.id)
			else -> NodeServiceResults.NodeUpdated(node)
		}
	}

	override suspend fun delete(node: Node): ServiceResult {
		val result = collection.deleteOne(Filters.eq(MongoNode::_id, node.objectId))
		collection.updateMany(
			Filters.eq(MongoNode::flowId, node.flowId.objectId),
			Updates.pull(MongoNode::next, node.objectId)
		)
		return when {
			result.deletedCount == 0L -> NodeServiceResults.NodeNotFoundError(node.id)
			else -> NodeServiceResults.NodeDeleted(node)
		}
	}
}
