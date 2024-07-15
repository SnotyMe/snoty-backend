package me.snoty.backend.integration.flow

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.utils.SettingsLookup
import me.snoty.backend.utils.orNull
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.graph.Graph
import me.snoty.integration.common.wiring.graph.GraphNode
import me.snoty.integration.common.wiring.graph.toRelational

interface FlowBuilder {
	/**
	 * Creates a flow from a graph node.
	 * This involves recursively looking up `next` nodes in the graph and creating a flow node for each of them.
	 */
	fun createFlowFromGraph(graph: Graph): List<RelationalFlowNode>
}

class FlowBuilderImpl(val settingsLookup: SettingsLookup) : FlowBuilder {
	/**
	 * Creates a flow from a graph node.
	 * This involves recursively looking up `next` nodes in the graph and creating a flow node for each of them.
	 */
	override fun createFlowFromGraph(graph: Graph): List<RelationalFlowNode> {
		val involvedNodes = graph.involvedNodes.associateBy { it._id }
		val rootNext  = graph.rootNext.mapNotNull { involvedNodes[it] }

		return rootNext.map {
			createFlowNode(it, involvedNodes, visitedNodes = listOf(it))
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
	): RelationalFlowNode {
		val next = graphNode.next
			?.mapNotNull { involvedNodes[it] }
			?.filter { it !in visitedNodes }
			?.map { createFlowNode(it, involvedNodes, visitedNodes + it) }
			?.orNull()

		val settings = settingsLookup(graphNode)
		return graphNode.toRelational(settings, next ?: emptyList())
	}
}
