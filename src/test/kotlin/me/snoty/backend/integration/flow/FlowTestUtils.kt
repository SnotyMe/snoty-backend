package me.snoty.backend.integration.flow

import me.snoty.backend.dev.randomString
import me.snoty.backend.test.node
import me.snoty.backend.test.nodeMetadata
import me.snoty.backend.utils.randomV7
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.flow.WorkflowSettings
import me.snoty.integration.common.wiring.flow.WorkflowWithNodes
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.simpleOutput
import kotlin.uuid.Uuid

fun relationalFlow(
	vararg nodes: FlowNode,
) = WorkflowWithNodes(
	_id = randomString(),
	userId = Uuid.randomV7(),
	name = randomString(),
	settings = WorkflowSettings(),
	nodes = nodes.toList(),
)

object EmitHandler : NodeHandler {
	val descriptor = NodeDescriptor(
		javaClass.packageName,
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
	makeId = ::randomString,
)
