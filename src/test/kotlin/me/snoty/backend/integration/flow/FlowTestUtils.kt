package me.snoty.backend.integration.flow

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import java.util.*

fun relationalFlow(
	descriptor: NodeDescriptor,
	settings: NodeSettings = EmptyNodeSettings(),
	next: List<RelationalFlowNode> = emptyList(),
) = RelationalFlowNode(
	NodeId(),
	UUID.randomUUID(),
	descriptor,
	settings,
	next
)
