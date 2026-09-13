package me.snoty.integration.common.wiring.node.template

import kotlinx.serialization.Serializable
import me.snoty.core.node.NodeType

@Serializable
data class NodeTemplate(
	val node: NodeType,
	val name: String,
	val template: String,
)
