package me.snoty.backend.integration.flow

import me.snoty.backend.dev.randomString
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.test.nodeMetadata
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.flow.WorkflowWithNodes
import me.snoty.integration.common.wiring.node.*
import me.snoty.integration.common.wiring.simpleOutput
import java.util.*

fun relationalFlow(
	vararg nodes: FlowNode,
) = WorkflowWithNodes(
	_id = NodeId(),
	userId = UUID.randomUUID(),
	name = randomString(),
	nodes = nodes.toList(),
)

object EmitHandler : NodeHandler {
	val descriptor = NodeDescriptor(
		Subsystem.INTEGRATION,
		"emit"
	)
	val metadata = nodeMetadata(
		descriptor,
		NodePosition.START,
	)

	override suspend fun NodeHandleContext.process(node: Node, input: NodeInput)
		= simpleOutput("test")
}
fun NodeRegistry.registerEmitHandler() {
	registerHandler(EmitHandler.metadata, EmitHandler)
}
fun emitNode(vararg next: FlowNode) = node(
	descriptor = EmitHandler.descriptor,
	next = next.toList(),
)

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
	settings = settings,
	next = next.map(FlowNode::_id),
)
