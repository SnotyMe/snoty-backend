package me.snoty.backend.integration.flow.model

import me.snoty.backend.integration.config.flow.NodeId
import org.bson.Document

/**
 * High-level representation of a flow node.
 */
data class FlowNode(
	val id: NodeId,
	val type: String,
	val config: Document,
	val next: List<FlowNode> = emptyList()
)
