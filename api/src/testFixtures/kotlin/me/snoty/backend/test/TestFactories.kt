package me.snoty.backend.test

import me.snoty.backend.wiring.node.EmptyNodeSettings
import me.snoty.backend.wiring.node.NodePosition
import me.snoty.backend.wiring.node.NodeSettings
import me.snoty.core.flow.FlowId
import me.snoty.core.node.FlowNode
import me.snoty.core.node.NodeId
import me.snoty.core.node.NodeType
import me.snoty.core.user.UserId
import kotlin.time.Clock
import kotlin.uuid.Uuid

fun node(
	type: String,
	name: String = type,
	settings: NodeSettings = EmptyNodeSettings(),
	next: List<FlowNode> = emptyList(),
	userId: UserId = UserId(Uuid.generateV7().toString()),
	makeId: () -> String = ::randomString,
) = FlowNode(
	id = NodeId(makeId()),
	flowId = FlowId(makeId()),
	userId = userId,
	type = NodeType(type),
	name = name,
	position = NodePosition(0, 0, 300, 200),
	logLevel = null,
	settings = settings,
	createdAt = Clock.System.now(),
	modifiedAt = Clock.System.now(),
	next = next.map(FlowNode::id),
)
