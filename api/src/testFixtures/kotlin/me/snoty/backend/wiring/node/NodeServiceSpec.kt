package me.snoty.backend.wiring.node

import me.snoty.backend.test.NoOpNodeHandler
import me.snoty.backend.test.nodeMetadata
import me.snoty.core.flow.FlowId
import me.snoty.core.node.NodeType
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.model.NodePosition

abstract class NodeServiceSpec {
	abstract val service: NodeService
	abstract val makeFlowId: suspend () -> FlowId

	private val type = NodeType("mytype")

	protected val nodeRegistry = NodeRegistryImpl().apply {
		registerHandler(nodeMetadata(type, position = NodePosition.START), NoOpNodeHandler)
	}
	
	// TODO: test other methods
}
