package me.snoty.backend.integration.flow

import me.snoty.backend.dev.randomString
import me.snoty.backend.test.node
import me.snoty.backend.test.nodeMetadata
import me.snoty.backend.utils.randomV7
import me.snoty.core.flow.FlowId
import me.snoty.core.flow.WorkflowSettings
import me.snoty.core.flow.WorkflowWithNodes
import me.snoty.core.node.FlowNode
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.user.UserId
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeRegistry
import kotlin.time.Clock
import kotlin.uuid.Uuid

fun relationalFlow(
	vararg nodes: FlowNode,
) = WorkflowWithNodes(
	id = FlowId(randomString()),
	userId = UserId(Uuid.randomV7().toString()),
	name = randomString(),
	settings = WorkflowSettings(),
	createdAt = Clock.System.now(),
	modifiedAt = Clock.System.now(),
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

	context(_: NodeHandleContext)
	override suspend fun process(node: NodeWithSettings, input: NodeInput) = input
}
fun NodeRegistry.registerEmitHandler() {
	registerHandler(EmitHandler.metadata, EmitHandler)
}
fun emitNode(vararg next: FlowNode) = node(
	descriptor = EmitHandler.descriptor,
	next = next.toList(),
	makeId = ::randomString,
)
