package me.snoty.backend.wiring.node.registry

import me.snoty.backend.wiring.node.NodeHandler
import me.snoty.backend.wiring.node.metadata.NodeMetadata
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeType
import org.koin.core.annotation.Single

@Single
class NodeRegistryImpl : NodeRegistry {
	/**
	 * Handlers that can be used for specific nodes.
	 */
	private val handlers: MutableMap<NodeType, NodeHandler> = mutableMapOf()
	private val metadatas: MutableMap<NodeType, NodeMetadata> = mutableMapOf()

	override fun lookupHandler(nodeType: NodeType): NodeHandler? {
		return handlers[nodeType]
	}

	override fun registerHandler(metadata: NodeMetadata, handler: NodeHandler) {
		val nodeType = metadata.type
		handlers[nodeType] = handler
		metadatas[nodeType] = metadata
	}

	override fun getHandlers(): Map<NodeType, NodeHandler> {
		return handlers
	}

	override fun getMetadata(): Map<NodeType, NodeMetadata> {
		return metadatas
	}

	override fun lookupTypesByStereotype(stereotype: NodeStereotype): List<NodeType> {
		return metadatas.mapNotNull { (nodeType, metadata) ->
			if (metadata.stereotype == stereotype) {
				nodeType
			} else {
				null
			}
		}
	}
}
