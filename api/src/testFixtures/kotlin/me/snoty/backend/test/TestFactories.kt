package me.snoty.backend.test

import me.snoty.core.flow.FlowId
import me.snoty.core.node.FlowNode
import me.snoty.core.node.NodeId
import me.snoty.core.user.UserId
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeSettings
import kotlin.time.Clock
import kotlin.uuid.Uuid

fun node(
	descriptor: NodeDescriptor,
	name: String = descriptor.name,
	settings: NodeSettings = EmptyNodeSettings(),
	next: List<FlowNode> = emptyList(),
	userId: UserId = UserId(Uuid.generateV7().toString()),
	makeId: () -> String = ::randomString,
) = FlowNode(
	id = NodeId(makeId()),
	flowId = FlowId(makeId()),
	userId = userId,
	descriptor = descriptor,
	name = name,
	position = NodePosition(0, 0, 300, 200),
	logLevel = null,
	settings = settings,
	createdAt = Clock.System.now(),
	modifiedAt = Clock.System.now(),
	next = next.map(FlowNode::id),
)
