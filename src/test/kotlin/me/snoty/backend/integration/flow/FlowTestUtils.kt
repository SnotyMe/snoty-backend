package me.snoty.backend.integration.flow

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import org.bson.Document
import java.util.*

fun relationalFlow(
	descriptor: NodeDescriptor,
	config: Document = Document(),
	next: List<RelationalFlowNode> = emptyList(),
) = RelationalFlowNode(
	NodeId(),
	UUID.randomUUID(),
	descriptor,
	config,
	next
)
