package me.snoty.integration.common.wiring.node.template

import kotlinx.serialization.Serializable
import me.snoty.integration.common.wiring.node.NodeDescriptor

@Serializable
data class NodeTemplate(
	val node: NodeDescriptor,
	val name: String,
	val template: String,
)
