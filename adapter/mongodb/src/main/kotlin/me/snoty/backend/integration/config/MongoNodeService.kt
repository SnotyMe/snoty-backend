package me.snoty.backend.integration.config

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.snoty.backend.database.mongo.deserializeOrInvalid
import me.snoty.backend.database.mongo.objectId
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.utils.bson.encode
import me.snoty.backend.wiring.node.MongoNode
import me.snoty.backend.wiring.node.NodeSettingsDeserializationService
import me.snoty.backend.wiring.node.toRelational
import me.snoty.backend.wiring.node.toStandalone
import me.snoty.core.FlowId
import me.snoty.core.Node
import me.snoty.core.NodeId
import me.snoty.core.UserId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.config.NodeServiceResults
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.flow.NODE_COLLECTION_NAME
import me.snoty.integration.common.wiring.flow.Workflow
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.conversions.Bson
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@Single
class MongoNodeService(
	db: MongoDatabase,
	private val settingsDeserializationService: NodeSettingsDeserializationService,
) : NodeService {
	private val collection = db.getCollection<MongoNode>(NODE_COLLECTION_NAME)

	override suspend fun get(userId: UserId?, id: NodeId): StandaloneNode? {
		val mongoNode = collection.find(
			Filters.and(
				if (userId != null) Filters.eq(MongoNode::userId.name, userId) else Filters.empty(),
				Filters.eq(MongoNode::_id.name, id.objectId),
			)
		).firstOrNull() ?: return null

		val settings = settingsDeserializationService.deserializeOrInvalid(mongoNode)
		return mongoNode.toStandalone(settings)
	}

	override fun getByFlow(flowId: FlowId): Flow<FlowNode> = collection.find(
		Filters.eq(MongoNode::flowId.name, flowId.objectId)
	).map { node ->
		val settings = settingsDeserializationService.deserializeOrInvalid(node)
		node.toRelational(settings)
	}

	override suspend fun <S : NodeSettings> create(
		userId: UserId,
		flow: Workflow,
		descriptor: NodeDescriptor,
		position: NodePosition,
		settings: S,
	): StandaloneNode {
		val node = MongoNode(
			flowId = flow.objectId,
			userId = userId,
			descriptor = descriptor,
			position = position,
			settings = collection.codecRegistry.encode(settings),
			next = emptyList()
		)

		collection.insertOne(node)

		return node.toStandalone(settings)
	}

	override suspend fun connect(from: Node, to: Node): ServiceResult {
		collection.updateOne(
			Filters.eq(MongoNode::_id.name, from.objectId),
			Updates.addToSet(MongoNode::next.name, to.objectId)
		)

		return NodeServiceResults.NodeConnected(from, to)
	}

	override suspend fun disconnect(from: Node, to: Node): ServiceResult {
		collection.updateOne(
			Filters.eq(MongoNode::_id.name, from.objectId),
			Updates.pull(MongoNode::next.name, to.objectId)
		)

		return NodeServiceResults.NodeDisconnected(from, to)
	}

	override suspend fun updatePosition(node: Node, position: NodePosition) = updateNode(
		node,
		Updates.set(MongoNode::position.name, position)
	)

	override suspend fun updateSettings(node: Node, settings: NodeSettings) = updateNode(
		node,
		Updates.set(MongoNode::settings.name, collection.codecRegistry.encode(settings))
	)

	override suspend fun updateLogLevel(node: Node, logLevel: Level?) = updateNode(
		node,
		when {
			logLevel != null -> Updates.set(MongoNode::logLevel.name, logLevel)
			else -> Updates.unset(MongoNode::logLevel.name)
		}
	)

	private suspend fun updateNode(node: Node, update: Bson): ServiceResult {
		val result = collection.updateOne(
			Filters.eq(MongoNode::_id.name, node.objectId),
			update
		)
		return when {
			result.matchedCount == 0L -> NodeServiceResults.NodeNotFoundError(node.id)
			else -> NodeServiceResults.NodeUpdated(node)
		}
	}

	override suspend fun delete(node: Node): ServiceResult {
		val result = collection.deleteOne(Filters.eq(MongoNode::_id.name, node.objectId))
		collection.updateMany(
			Filters.eq(MongoNode::flowId.name, node.flowId.objectId),
			Updates.pull(MongoNode::next.name, node.objectId)
		)
		return when {
			result.deletedCount == 0L -> NodeServiceResults.NodeNotFoundError(node.id)
			else -> NodeServiceResults.NodeDeleted(node)
		}
	}
}
