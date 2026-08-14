package me.snoty.core

import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import org.slf4j.event.Level

interface Node {
	val id: NodeId
	val flowId: FlowId
	val userId: UserId
	val descriptor: NodeDescriptor
	val logLevel: Level?
	val position: NodePosition
}
