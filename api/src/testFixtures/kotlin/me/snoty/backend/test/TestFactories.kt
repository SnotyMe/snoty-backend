package me.snoty.backend.test

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.utils.randomV7
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import kotlin.uuid.Uuid

fun node(
	descriptor: NodeDescriptor,
	settings: NodeSettings = EmptyNodeSettings(),
	next: List<FlowNode> = emptyList(),
	userId: String = Uuid.randomV7().toString(),
	makeId: () -> NodeId = ::randomString,
) = FlowNode(
	_id = makeId(),
	flowId = makeId(),
	userId = userId,
	descriptor = descriptor,
	logLevel = null,
	settings = settings,
	next = next.map(FlowNode::_id),
)
