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
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.flow.FLOW_COLLECTION_NAME
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.graph.Graph
import me.snoty.integration.common.wiring.graph.GraphNode
import me.snoty.integration.common.wiring.toRelational
import org.koin.core.annotation.Single
import org.slf4j.Logger

@Single
open class MongoFlowService(
	db: MongoDatabase,
	private val builder: FlowBuilder,
	private val runner: FlowRunner
) : FlowService {
	protected val collection = db.getCollection<GraphNode>(FLOW_COLLECTION_NAME)

	override fun getFlowForNode(node: Node): Flow<RelationalFlowNode> {
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
			val next = builder.createFlowFromGraph(graph)
			node.toRelational(next)
		}
	}

	override fun runFlow(
		jobId: String,
		logger: Logger,
		node: Node,
		input: IntermediateData
	): Flow<Unit> = getFlowForNode(node)
		.flatMapMerge {
			if (logger.isDebugEnabled)
				logger.debug("Processing node {} with input {}", visualizeFlow(listOf(it)), input)
			runner.execute(jobId, logger, it, input)
		}
}
