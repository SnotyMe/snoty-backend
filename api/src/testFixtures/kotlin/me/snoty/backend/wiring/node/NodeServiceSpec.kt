package me.snoty.backend.wiring.node

import me.snoty.backend.test.NoOpNodeHandler
import me.snoty.backend.test.nodeMetadata
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.backend.wiring.node.registry.NodeRegistryImpl
import me.snoty.core.flow.FlowId
import me.snoty.core.node.NodeType

abstract class NodeServiceSpec {
	abstract val service: NodeService
	abstract val makeFlowId: suspend () -> FlowId

	private val type = NodeType("mytype")

	protected val nodeRegistry = NodeRegistryImpl().apply {
		registerHandler(nodeMetadata(type, stereotype = NodeStereotype.START), NoOpNodeHandler)
	}
	
	// TODO: test other methods
}
