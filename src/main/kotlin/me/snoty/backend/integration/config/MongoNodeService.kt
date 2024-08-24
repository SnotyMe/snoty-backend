package me.snoty.backend.integration.config

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.snoty.backend.database.mongo.decode
import me.snoty.backend.database.mongo.encode
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.config.NodeServiceResults
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.flow.FLOW_COLLECTION_NAME
import me.snoty.integration.common.wiring.graph.GraphNode
import me.snoty.integration.common.wiring.graph.toStandalone
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import org.koin.core.annotation.Single
import java.util.*

@Single
class MongoNodeService(
	db: MongoDatabase,
	private val nodeRegistry: NodeRegistry,
) : NodeService {
	private val collection = db.getCollection<GraphNode>(FLOW_COLLECTION_NAME)

	private fun mapSettings(node: GraphNode): NodeSettings {
		val handler = nodeRegistry.lookupHandler(node.descriptor) ?: throw IllegalArgumentException("No handler found for node ${node.descriptor}")
		return collection.codecRegistry.decode(handler.metadata.settingsClass, node.settings)
	}

	override fun query(userID: UUID?, position: NodePosition?): Flow<StandaloneNode> {
		val filters = listOf(
			buildUserIDFilter(userID),
			// pre-filter if no nodes with this position exist
			buildPositionFilter(nodeRegistry, position) ?: return emptyFlow(),
		)

		return collection
			.find<GraphNode>(Filters.and(filters))
			.map { node ->
				val settings = mapSettings(node)
				node.toStandalone(settings)
			}
	}

	override suspend fun get(id: NodeId): StandaloneNode? {
		val graphNode = collection.find(
			Filters.eq(GraphNode::_id.name, id)
		).firstOrNull() ?: return null

		val settings = mapSettings(graphNode)
		return graphNode.toStandalone(settings)
	}

	override suspend fun <S : NodeSettings> create(userID: UUID, descriptor: NodeDescriptor, settings: S): StandaloneNode {
		val node = GraphNode(
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
			Filters.eq(GraphNode::_id.name, fromNode._id),
			Updates.addToSet(GraphNode::next.name, toNode._id)
		)

		return NodeServiceResults.NodeConnected(from, to)
	}

	override suspend fun disconnect(from: NodeId, to: NodeId): ServiceResult {
		val fromNode = get(from) ?: return NodeServiceResults.NodeNotFoundError(from)
		val toNode = get(to) ?: return NodeServiceResults.NodeNotFoundError(to)

		collection.updateOne(
			Filters.eq(GraphNode::_id.name, fromNode._id),
			Updates.pull(GraphNode::next.name, toNode._id)
		)

		return NodeServiceResults.NodeDisconnected(from, to)
	}

	override suspend fun updateSettings(id: NodeId, settings: NodeSettings): ServiceResult {
		val result = collection.updateOne(
			Filters.eq(GraphNode::_id.name, id),
			Updates.set(GraphNode::settings.name, collection.codecRegistry.encode(settings))
		)
		return when {
			result.matchedCount == 0L -> NodeServiceResults.NodeNotFoundError(id)
			else -> NodeServiceResults.NodeSettingsUpdated(id)
		}
	}
}
