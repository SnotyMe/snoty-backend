package me.snoty.backend.wiring.node

import me.snoty.backend.test.NoOpNodeHandler
import me.snoty.backend.test.nodeMetadata
import me.snoty.core.FlowId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.node.NodeDescriptor

abstract class NodeServiceSpec {
	abstract val service: NodeService
	abstract val makeFlowId: suspend () -> FlowId

	private val descriptor = NodeDescriptor(
		namespace = javaClass.packageName,
		name = "mytype"
	)

	protected val nodeRegistry = NodeRegistryImpl().apply {
		registerHandler(nodeMetadata(descriptor, position = NodePosition.START), NoOpNodeHandler)
	}
	
	// TODO: test other methods
}
