package me.snoty.backend.integration.flow.node

import me.snoty.backend.integration.flow.model.NodeDescriptor

class NodeRegistryImpl : NodeRegistry {
	/**
	 * Handlers that can be used for specific nodes.
	 */
	private val handlers: MutableMap<NodeDescriptor, NodeHandler> = mutableMapOf()

	/**
	 * Handlers that can be used for all nodes in a subsystem.
	 * Useful for chaining, e.g. offloading all integration nodes to the IntegrationRegistry
	 */
	private val subsystemHandlers: MutableMap<String, NodeHandler> = mutableMapOf()

	override fun lookupHandler(descriptor: NodeDescriptor): NodeHandler? {
		return handlers[descriptor] ?: subsystemHandlers[descriptor.subsystem]
	}

	override fun registerHandler(descriptor: NodeDescriptor, handler: NodeHandler) {
		handlers[descriptor] = handler
	}

	override fun registerSubsystemHandler(subsystem: String, handler: NodeHandler) {
		subsystemHandlers[subsystem] = handler
	}
}
