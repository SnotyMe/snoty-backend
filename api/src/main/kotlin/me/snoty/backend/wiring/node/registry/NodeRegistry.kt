package me.snoty.backend.wiring.node.registry

import me.snoty.backend.wiring.node.NodeHandler
import me.snoty.backend.wiring.node.metadata.NodeMetadata
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeType

interface NodeRegistry {
	fun lookupHandler(nodeType: NodeType): NodeHandler?

	/**
	 * Highly specific function that returns all types for nodes of a given stereotype.
	 * This is useful for building a database query for all start nodes, for example.
	 */
	fun lookupTypesByStereotype(stereotype: NodeStereotype): List<NodeType>

	fun registerHandler(metadata: NodeMetadata, handler: NodeHandler)

	fun getHandlers(): Map<NodeType, NodeHandler>
	fun getMetadata(): Map<NodeType, NodeMetadata>

	fun getMetadataOrNull(type: NodeType): NodeMetadata? = getMetadata()[type]
	fun getMetadata(nodeType: NodeType): NodeMetadata =
		getMetadataOrNull(nodeType) ?: throw IllegalArgumentException("No metadata found for $nodeType")
}
