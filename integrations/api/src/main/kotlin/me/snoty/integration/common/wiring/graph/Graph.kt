package me.snoty.integration.common.wiring.graph

import me.snoty.backend.integration.config.flow.NodeId

/**
 * A graph is a collection of nodes.
 */
data class Graph(
	val rootNext: List<NodeId>,
	val involvedNodes: List<GraphNode>
)
