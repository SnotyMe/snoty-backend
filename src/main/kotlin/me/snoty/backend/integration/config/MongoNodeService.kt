package me.snoty.backend.integration.config

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.snoty.backend.database.mongo.encode
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.utils.SettingsLookup
import me.snoty.backend.scheduling.node.NodeScheduler
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.config.NodeServiceResults
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.flow.FLOW_COLLECTION_NAME
import me.snoty.integration.common.wiring.graph.GraphNode
import me.snoty.integration.common.wiring.graph.toStandalone
import me.snoty.integration.common.wiring.node.*
import org.bson.conversions.Bson
import java.util.*

class MongoNodeService(
	db: MongoDatabase,
	private val nodeRegistry: NodeRegistry,
	private val scheduler: NodeScheduler,
	private val settingsLookup: SettingsLookup
) : NodeService {
	private val collection = db.getCollection<GraphNode>(FLOW_COLLECTION_NAME)

	override fun getByUser(userID: UUID, position: NodePosition?): Flow<StandaloneNode> {
		val additionalFilters: Bson = when (position) {
			null -> Filters.empty()
			else -> {
				val filters = nodeRegistry.lookupDescriptorsByPosition(position).map {
					val prefix = GraphNode::descriptor.name + "."
					Filters.and(
						Filters.eq(prefix + NodeDescriptor::type.name, it.type),
						Filters.eq(prefix + NodeDescriptor::subsystem.name, it.subsystem)
					)
				}

				when {
					// zero nodes with this position => zero results
					filters.isEmpty() -> return emptyFlow()
					else -> Filters.or(filters)
				}
			}
		}
		return collection.find<GraphNode>(
			Filters.and(
				Filters.eq(GraphNode::userId.name, userID),
				additionalFilters
			)
		).map { node ->
			val settings = settingsLookup(node)
			node.toStandalone(settings)
		}
	}

	override fun getAll(integrationType: String): Flow<StandaloneNode> {
		return collection.find<StandaloneNode>(
			NodeDescriptor.filter(Subsystem.INTEGRATION, integrationType)
		)
	}

	override suspend fun get(id: NodeId): StandaloneNode? {
		val graphNode = collection.find(
			Filters.eq(GraphNode::_id.name, id)
		).firstOrNull() ?: return null

		val settings = settingsLookup(graphNode)
		return graphNode.toStandalone(settings)
	}

	override suspend fun <S : NodeSettings> create(userID: UUID, descriptor: NodeDescriptor, settings: S): NodeId {
		val handler = nodeRegistry.lookupHandler(descriptor)
			?: throw IllegalArgumentException("Handler not found for $descriptor")

		val node = GraphNode(
			userId = userID,
			descriptor = descriptor,
			settings = collection.codecRegistry.encode(settings),
			next = emptyList()
		)
		collection.insertOne(node)

		if (handler.position == NodePosition.START) {
			scheduler.schedule(node.toStandalone(settings))
		}
		return node._id
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
