package me.snoty.backend.test

import me.snoty.backend.utils.randomV7
import me.snoty.core.FlowId
import me.snoty.core.NodeId
import me.snoty.core.UserId
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import kotlin.uuid.Uuid

fun node(
	descriptor: NodeDescriptor,
	settings: NodeSettings = EmptyNodeSettings(),
	next: List<FlowNode> = emptyList(),
	userId: UserId = UserId(Uuid.randomV7().toString()),
	makeId: () -> String = ::randomString,
) = FlowNode(
	_id = NodeId(makeId()),
	flowId = FlowId(makeId()),
	userId = userId,
	descriptor = descriptor,
	logLevel = null,
	settings = settings,
	next = next.map(FlowNode::_id),
)
