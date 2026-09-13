package me.snoty.integration.common.wiring.node

import me.snoty.core.node.NodeType
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.NodeMetadata

interface NodeRegistry {
	fun lookupHandler(nodeType: NodeType): NodeHandler?

	/**
	 * Highly specific function that returns all types for nodes of a given position.
	 * This is useful for building a database query for all start nodes, for example.
	 */
	fun lookupTypesByPosition(position: NodePosition): List<NodeType>

	fun registerHandler(metadata: NodeMetadata, handler: NodeHandler)

	fun getHandlers(): Map<NodeType, NodeHandler>
	fun getMetadata(): Map<NodeType, NodeMetadata>

	fun getMetadataOrNull(type: NodeType): NodeMetadata? = getMetadata()[type]
	fun getMetadata(nodeType: NodeType): NodeMetadata =
		getMetadataOrNull(nodeType) ?: throw IllegalArgumentException("No metadata found for $nodeType")
}
