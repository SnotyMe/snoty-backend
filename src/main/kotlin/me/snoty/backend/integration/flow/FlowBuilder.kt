package me.snoty.backend.integration.flow

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.model.FlowNode
import me.snoty.backend.integration.flow.model.graph.Graph
import me.snoty.backend.integration.flow.model.graph.GraphNode
import me.snoty.backend.utils.orNull

interface FlowBuilder {
	/**
	 * Creates a flow from a graph node.
	 * This involves recursively looking up `next` nodes in the graph and creating a flow node for each of them.
	 */
	fun createFlowFromGraph(graph: Graph): List<FlowNode>
}

object FlowBuilderImpl : FlowBuilder {
	/**
	 * Creates a flow from a graph node.
	 * This involves recursively looking up `next` nodes in the graph and creating a flow node for each of them.
	 */
	override fun createFlowFromGraph(graph: Graph): List<FlowNode> {
		val involvedNodes = graph.involvedNodes.associateBy { it._id }
		val rootNext  = graph.rootNext.mapNotNull { involvedNodes[it] }

		return rootNext.map {
			// start with `graph.rootNext` to avoid circling back to `next` of `root`
			createFlowNode(it, involvedNodes, rootNext)
		}
	}

	/**
	 * Creates a flow node from a graph node.
	 * Recursively looks up `next` nodes of the current node in the graph to build a de-normalized flow.
	 */
	private fun createFlowNode(
		graphNode: GraphNode,
		involvedNodes: Map<NodeId, GraphNode>,
		visitedNodes: List<GraphNode>
	): FlowNode {
		val next = graphNode.next
			?.mapNotNull { involvedNodes[it] }
			?.filter { it !in visitedNodes }
			?.map { createFlowNode(it, involvedNodes, visitedNodes + it) }
			?.orNull()
		return FlowNode(graphNode._id, graphNode.type, graphNode.config, next ?: emptyList())
	}
}
