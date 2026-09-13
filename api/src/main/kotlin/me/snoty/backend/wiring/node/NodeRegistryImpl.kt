package me.snoty.backend.wiring.node

import me.snoty.core.node.NodeType
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeRegistry
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

	override fun lookupTypesByPosition(position: NodePosition): List<NodeType> {
		return metadatas.mapNotNull { (nodeType, metadata) ->
			if (metadata.position == position) {
				nodeType
			} else {
				null
			}
		}
	}
}
