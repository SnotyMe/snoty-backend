package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.model.NodePosition

interface NodeRegistry {
	fun lookupHandler(descriptor: NodeDescriptor): NodeHandler?

	/**
	 * Highly specific function that returns all descriptors for nodes of a given position.
	 * This is useful for building a database query for all start nodes, for example.
	 */
	fun lookupDescriptorsByPosition(position: NodePosition): List<NodeDescriptor>

	fun registerHandler(metadata: NodeMetadata, handler: NodeHandler)

	fun getHandlers(): Map<NodeDescriptor, NodeHandler>
	fun getMetadata(): Map<NodeDescriptor, NodeMetadata>
	fun getMetadata(descriptor: NodeDescriptor): NodeMetadata {
		return getMetadata()[descriptor] ?: throw IllegalArgumentException("No metadata found for $descriptor")
	}
}
