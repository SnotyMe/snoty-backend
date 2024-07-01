package me.snoty.backend.integration.flow.node

import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeRegistry

class NodeRegistryImpl : NodeRegistry {
	/**
	 * Handlers that can be used for specific nodes.
	 */
	private val handlers: MutableMap<NodeDescriptor, NodeHandler> = mutableMapOf()

	override fun lookupHandler(descriptor: NodeDescriptor): NodeHandler? {
		return handlers[descriptor]
	}

	override fun registerHandler(descriptor: NodeDescriptor, handler: NodeHandler) {
		handlers[descriptor] = handler
	}

	override fun getHandlers(): Map<NodeDescriptor, NodeHandler> {
		return handlers
	}

	override fun lookupDescriptorsByPosition(position: NodePosition): List<NodeDescriptor> {
		// TODO: consider caching this
		return handlers.filter { it.value.position == position }.keys.toList()
	}
}
