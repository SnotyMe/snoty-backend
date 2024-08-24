package me.snoty.integration.common.wiring

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeSettings
import java.util.*

/**
 * Node interface without any specifics regarding settings.
 * No relations are guaranteed.
 */
interface GenericNode {
	val _id: NodeId
	val userId: UUID
	val descriptor: NodeDescriptor
}

/**
 * Node interface with already serialized NodeSettings
 */
interface Node : GenericNode {
	val settings: NodeSettings
}

/**
 * High-level representation of a node in the context of a flow.
 * Contains a list of the next nodes in the flow.
 */
@Serializable
data class RelationalFlowNode(
	override val _id: NodeId,
	@Contextual
	override val userId: UUID,
	override val descriptor: NodeDescriptor,
	override val settings: NodeSettings,
	val next: List<RelationalFlowNode> = emptyList()
) : Node

fun Node.toRelational(next: List<RelationalFlowNode>) = RelationalFlowNode(
	_id = _id,
	userId = userId,
	descriptor = descriptor,
	settings = settings,
	next = next
)

@Serializable
data class StandaloneNode(
	override val _id: NodeId,
	@Contextual
	override val userId: UUID,
	override val descriptor: NodeDescriptor,
	@Contextual
	override val settings: NodeSettings,
) : Node

inline fun <reified T : NodeSettings> Node.getConfig(): T {
	return settings as T
}
