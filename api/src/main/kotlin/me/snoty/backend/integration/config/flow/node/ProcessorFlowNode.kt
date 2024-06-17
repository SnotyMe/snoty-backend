package me.snoty.backend.integration.config.flow.node

import me.snoty.backend.integration.config.flow.FlowNode
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.config.mapping.Mappable

/**
 * Maps from A to B
 */
data class ProcessorFlowNode(
	val mappings: Map<String, Mappable>,
	override val id: NodeId = NodeId()
) : FlowNode
