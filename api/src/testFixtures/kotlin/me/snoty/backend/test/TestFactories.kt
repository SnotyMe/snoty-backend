package me.snoty.backend.test

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import java.util.*

fun node(
	descriptor: NodeDescriptor,
	settings: NodeSettings = EmptyNodeSettings(),
	next: List<FlowNode> = emptyList(),
	userId: UUID = UUID.randomUUID(),
) = FlowNode(
	_id = NodeId(),
	flowId = NodeId(),
	userId = userId,
	descriptor = descriptor,
	logLevel = null,
	settings = settings,
	next = next.map(FlowNode::_id),
)
