package me.snoty.backend.integration.config

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import me.snoty.backend.database.mongo.encode
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.node.NodeScheduler
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.config.NodeServiceResults
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.StandaloneFlowNode
import me.snoty.integration.common.wiring.flow.FLOW_COLLECTION_NAME
import me.snoty.integration.common.wiring.graph.GraphNode
import me.snoty.integration.common.wiring.node.*
import org.bson.conversions.Bson
import java.util.*

class MongoNodeService(
	db: MongoDatabase,
	private val nodeRegistry: NodeRegistry,
	private val scheduler: NodeScheduler
) : NodeService {
	private val collection = db.getCollection<GraphNode>(FLOW_COLLECTION_NAME)

	override fun getByUser(userID: UUID, position: NodePosition?): Flow<IFlowNode> {
		val additionalFilters: Bson = when (position) {
			null -> Filters.empty()
			NodePosition.START -> Filters.or(
				nodeRegistry.lookupDescriptorsByPosition(position).map {
					val prefix = GraphNode::descriptor.name + "."
					Filters.and(
						Filters.eq(prefix + NodeDescriptor::type, it.type),
						Filters.eq(prefix + NodeDescriptor::subsystem, it.subsystem)
					)
				}
			)
			NodePosition.MIDDLE -> Filters.empty()
			NodePosition.END -> Filters.or(
				Filters.exists(GraphNode::next.name, false),
				Filters.size(GraphNode::next.name, 0)
			)
		}
		return collection.find<StandaloneFlowNode>(
			Filters.and(
				Filters.eq(GraphNode::userId.name, userID),
				additionalFilters
			)
		)
	}

	override fun getAll(integrationType: String): Flow<StandaloneFlowNode> {
		return collection.find<StandaloneFlowNode>(
			NodeDescriptor.filter(Subsystem.INTEGRATION, integrationType)
		)
	}

	override suspend fun get(id: NodeId): IFlowNode? {
		return collection.find(
			Filters.eq(GraphNode::_id.name, id)
		).firstOrNull()
	}

	override suspend fun <S : NodeSettings> create(userID: UUID, descriptor: NodeDescriptor, settings: S): NodeId {
		val node = GraphNode(
			userId = userID,
			descriptor = descriptor,
			config = collection.codecRegistry.encode(settings),
			next = emptyList()
		)
		collection.insertOne(node)
		// TODO: throw if handler doesn't exist?
		val handler = nodeRegistry.lookupHandler(descriptor)
		if (handler?.position == NodePosition.START) {
			scheduler.schedule(node)
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
}
