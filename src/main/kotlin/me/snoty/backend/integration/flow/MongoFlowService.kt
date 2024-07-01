package me.snoty.backend.integration.flow

import com.mongodb.client.model.Aggregates.graphLookup
import com.mongodb.client.model.Aggregates.match
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import me.snoty.backend.database.mongo.Aggregations.project
import me.snoty.backend.database.mongo.aggregate
import me.snoty.backend.integration.flow.FlowBuilderImpl.createFlowFromGraph
import me.snoty.integration.common.wiring.EdgeVertex
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.flow.FLOW_COLLECTION_NAME
import me.snoty.integration.common.wiring.flow.FlowOutput
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.graph.Graph
import me.snoty.integration.common.wiring.graph.GraphNode
import me.snoty.integration.common.wiring.toRelational
import org.slf4j.Logger

open class MongoFlowService(
	db: MongoDatabase,
	private val runner: FlowRunner
) : FlowService {
	protected val collection = db.getCollection<GraphNode>(FLOW_COLLECTION_NAME)

	override fun getFlowForNode(node: IFlowNode): Flow<RelationalFlowNode> {
		return collection.aggregate<Graph>(
			match(Filters.eq(GraphNode::_id.name, node._id)),
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
		).map { graph ->
			val next = createFlowFromGraph(graph)
			val relationalNode = node.toRelational(next)
			relationalNode
		}
	}

	override fun runFlow(logger: Logger, node: IFlowNode, input: EdgeVertex): Flow<FlowOutput>
		= getFlowForNode(node)
			.flatMapMerge {
				logger.debug("Processing node {} with input {}", visualizeFlow(listOf(it)), input)
				runner.execute(logger, it, input)
			}
}
