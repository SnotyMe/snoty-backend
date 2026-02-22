package me.snoty.integration.common.wiring

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import me.snoty.core.FlowId
import me.snoty.core.NodeId
import me.snoty.core.UserId
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import org.slf4j.event.Level

/**
 * Node interface without any specifics regarding settings.
 * No relations are guaranteed.
 */
interface GenericNode {
	val _id: NodeId
	val flowId: FlowId
	val userId: UserId
	val descriptor: NodeDescriptor
	val logLevel: Level?
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
data class FlowNode(
	override val _id: NodeId,
	override val flowId: FlowId,
	override val userId: UserId,
	override val descriptor: NodeDescriptor,
	override val logLevel: Level?,
	override val settings: NodeSettings,
	val next: List<NodeId> = emptyList()
) : Node

@Serializable
data class StandaloneNode(
	override val _id: NodeId,
	override val flowId: FlowId,
	override val userId: UserId,
	override val descriptor: NodeDescriptor,
	override val logLevel: Level?,
	@Contextual
	override val settings: NodeSettings,
) : Node

fun StandaloneNode.toRelational(next: List<NodeId>?) = FlowNode(
	_id = _id,
	flowId = flowId,
	userId = userId,
	descriptor = descriptor,
	logLevel = logLevel,
	settings = settings,
	next = next ?: emptyList(),
)

inline fun <reified T : NodeSettings> Node.getConfig(): T {
	return settings as T
}
