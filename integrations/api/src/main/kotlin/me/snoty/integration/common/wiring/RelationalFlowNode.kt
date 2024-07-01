package me.snoty.integration.common.wiring

import me.snoty.backend.database.mongo.decode
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.graph.GraphNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistry
import java.util.*

interface IFlowNode {
	val _id: NodeId
	val userId: UUID
	val descriptor: NodeDescriptor
	val config: Document
}

fun IFlowNode.toRelational(next: List<RelationalFlowNode>): RelationalFlowNode {
	return when (this) {
		is RelationalFlowNode -> this
		is StandaloneFlowNode, is GraphNode -> RelationalFlowNode(_id, userId, descriptor, config, next)
		else -> throw IllegalArgumentException("Unknown flow node type: $this")
	}
}

/**
 * High-level representation of a flow node.
 * Contains a list of the next nodes in the flow.
 */
data class RelationalFlowNode(
	override val _id: NodeId,
	override val userId: UUID,
	override val descriptor: NodeDescriptor,
	override val config: Document,
	val next: List<RelationalFlowNode> = emptyList()
) : IFlowNode

data class StandaloneFlowNode(
	override val _id: NodeId,
	override val userId: UUID,
	override val descriptor: NodeDescriptor,
	override val config: Document,
) : IFlowNode

context(NodeHandler)
inline fun <reified T : NodeSettings> IFlowNode.getConfig(codecRegistry: CodecRegistry): T {
	val nodeHandler = this@NodeHandler
	require(nodeHandler.settingsClass == T::class) {
		"Expected settings class ${nodeHandler.settingsClass}, got ${T::class}"
	}
	return codecRegistry.decode(T::class, config)
}
