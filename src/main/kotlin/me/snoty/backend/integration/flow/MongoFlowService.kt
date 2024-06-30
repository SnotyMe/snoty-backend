package me.snoty.backend.integration.flow

import com.mongodb.client.model.Aggregates.graphLookup
import com.mongodb.client.model.Aggregates.match
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import me.snoty.backend.database.mongo.Aggregations.project
import me.snoty.backend.database.mongo.aggregate
import me.snoty.backend.integration.flow.FlowBuilderImpl.createFlowFromGraph
import me.snoty.backend.integration.flow.model.FlowNode
import me.snoty.backend.integration.flow.model.graph.Graph
import me.snoty.backend.integration.flow.model.graph.GraphNode
import me.snoty.integration.common.flow.FLOW_COLLECTION_NAME

open class MongoFlowService(db: MongoDatabase) : FlowService {
	protected val collection = db.getCollection<GraphNode>(FLOW_COLLECTION_NAME)

	override fun getFlowForNode(node: FlowNode): Flow<FlowNode> {
		return collection.aggregate<Graph>(
			match(Filters.eq(GraphNode::_id.name, node.id)),
			graphLookup(
				/* from = */ collection.namespace.collectionName,
				/* startWith = */ "\$${GraphNode::_id.name}",
				/* connectFromField = */ GraphNode::next.name,
				/* connectToField = */ GraphNode::_id.name,
				/* as = */ Graph::involvedNodes.name
			),
			project(
				Projections.computed(Graph::rootNext.name, "\$${GraphNode::next.name}"),
				Projections.include(Graph::involvedNodes.name)
			)
		).transform { graph ->
			createFlowFromGraph(graph).forEach { node ->
				emit(node)
			}
		}
	}
}
