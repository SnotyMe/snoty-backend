package me.snoty.backend.integration.flow.node

import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.model.NodePosition
import org.koin.core.annotation.Single

@Single
class NodeRegistryImpl : NodeRegistry {
	/**
	 * Handlers that can be used for specific nodes.
	 */
	private val handlers: MutableMap<NodeDescriptor, NodeHandler> = mutableMapOf()
	private val metadatas: MutableMap<NodeDescriptor, NodeMetadata> = mutableMapOf()

	override fun lookupHandler(descriptor: NodeDescriptor): NodeHandler? {
		return handlers[descriptor]
	}

	override fun registerHandler(metadata: NodeMetadata, handler: NodeHandler) {
		val descriptor = metadata.descriptor
		handlers[descriptor] = handler
		metadatas[descriptor] = metadata
	}

	override fun getHandlers(): Map<NodeDescriptor, NodeHandler> {
		return handlers
	}

	override fun getMetadata(): Map<NodeDescriptor, NodeMetadata> {
		return metadatas
	}

	override fun lookupDescriptorsByPosition(position: NodePosition): List<NodeDescriptor> {
		return metadatas.mapNotNull { (descriptor, metadata) ->
			if (metadata.position == position) {
				descriptor
			} else {
				null
			}
		}
	}
}
