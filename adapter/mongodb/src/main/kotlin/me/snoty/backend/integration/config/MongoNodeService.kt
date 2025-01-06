package me.snoty.backend.integration.config

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.snoty.backend.database.mongo.deserializeOrInvalid
import me.snoty.backend.utils.bson.encode
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.wiring.node.NodeSettingsSerializationService
import me.snoty.backend.wiring.node.deserializeOrInvalid
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.config.NodeServiceResults
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.flow.NODE_COLLECTION_NAME
import me.snoty.backend.wiring.node.MongoNode
import me.snoty.backend.wiring.node.toRelational
import me.snoty.backend.wiring.node.toStandalone
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.types.ObjectId
import org.koin.core.annotation.Single
import org.slf4j.event.Level
import java.util.*

@Single
class MongoNodeService(
	db: MongoDatabase,
	private val nodeRegistry: NodeRegistry,
	private val settingsService: NodeSettingsSerializationService,
) : NodeService {
	private val collection = db.getCollection<MongoNode>(NODE_COLLECTION_NAME)

	override fun query(userID: UUID?, position: NodePosition?): Flow<StandaloneNode> {
		val filters = listOf(
			buildUserIDFilter(userID),
			// pre-filter if no nodes with this position exist
			buildPositionFilter(nodeRegistry, position) ?: return emptyFlow(),
		)

		return collection
			.find<MongoNode>(Filters.and(filters))
			.map { node ->
				val settings = settingsService.deserializeOrInvalid(node)
				node.toStandalone(settings)
			}
	}

	override suspend fun get(id: NodeId): StandaloneNode? {
		val mongoNode = collection.find(
			Filters.eq(MongoNode::_id.name, ObjectId(id))
		).firstOrNull() ?: return null

		val settings = settingsService.deserializeOrInvalid(mongoNode)
		return mongoNode.toStandalone(settings)
	}

	override fun getByFlow(flowId: NodeId): Flow<FlowNode> = collection.find(
		Filters.eq(MongoNode::flowId.name, ObjectId(flowId))
	).map { node ->
		val settings = settingsService.deserializeOrInvalid(node)
		node.toRelational(settings)
	}

	override suspend fun <S : NodeSettings> create(
		userID: UUID,
		flowId: NodeId,
		descriptor: NodeDescriptor,
		settings: S,
	): StandaloneNode {
		val node = MongoNode(
			flowId = ObjectId(flowId),
			userId = userID,
			descriptor = descriptor,
			settings = collection.codecRegistry.encode(settings),
			next = emptyList()
		)

		collection.insertOne(node)

		return node.toStandalone(settings)
	}

	override suspend fun connect(from: NodeId, to: NodeId): ServiceResult {
		val fromNode = get(from) ?: return NodeServiceResults.NodeNotFoundError(from)
		val toNode = get(to) ?: return NodeServiceResults.NodeNotFoundError(to)

		collection.updateOne(
			Filters.eq(MongoNode::_id.name, ObjectId(fromNode._id)),
			Updates.addToSet(MongoNode::next.name, ObjectId(toNode._id))
		)

		return NodeServiceResults.NodeConnected(from, to)
	}

	override suspend fun disconnect(from: NodeId, to: NodeId): ServiceResult {
		val fromNode = get(from) ?: return NodeServiceResults.NodeNotFoundError(from)
		val toNode = get(to) ?: return NodeServiceResults.NodeNotFoundError(to)

		collection.updateOne(
			Filters.eq(MongoNode::_id.name, ObjectId(fromNode._id)),
			Updates.pull(MongoNode::next.name, ObjectId(toNode._id))
		)

		return NodeServiceResults.NodeDisconnected(from, to)
	}

	override suspend fun updateSettings(id: NodeId, settings: NodeSettings): ServiceResult {
		val result = collection.updateOne(
			Filters.eq(MongoNode::_id.name, ObjectId(id)),
			Updates.set(MongoNode::settings.name, collection.codecRegistry.encode(settings))
		)
		return when {
			result.matchedCount == 0L -> NodeServiceResults.NodeNotFoundError(id)
			else -> NodeServiceResults.NodeSettingsUpdated(id)
		}
	}

	override suspend fun updateLogLevel(id: NodeId, logLevel: Level?): ServiceResult {
		val result = collection.updateOne(
			Filters.eq(MongoNode::_id.name, ObjectId(id)),
			when {
				logLevel != null -> Updates.set(MongoNode::logLevel.name, logLevel)
				else -> Updates.unset(MongoNode::logLevel.name)
			}
		)
		return when {
			result.matchedCount == 0L -> NodeServiceResults.NodeNotFoundError(id)
			else -> NodeServiceResults.NodeLogLevelUpdated(id)
		}
	}

	override suspend fun delete(id: NodeId): ServiceResult {
		val node = get(id) ?: return NodeServiceResults.NodeNotFoundError(id)

		val result = collection.deleteOne(Filters.eq(MongoNode::_id.name, ObjectId(id)))
		collection.updateMany(
			Filters.eq(MongoNode::flowId.name, ObjectId(node.flowId)),
			Updates.pull(MongoNode::next.name, ObjectId(id))
		)
		return when {
			result.deletedCount == 0L -> NodeServiceResults.NodeNotFoundError(id)
			else -> NodeServiceResults.NodeDeleted(id)
		}
	}
}
