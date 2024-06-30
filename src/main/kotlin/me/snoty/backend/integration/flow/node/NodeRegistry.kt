package me.snoty.backend.integration.flow.node

import me.snoty.backend.integration.flow.EdgeVertex
import me.snoty.backend.integration.flow.model.FlowNode
import me.snoty.backend.integration.flow.model.NodeDescriptor

/**
 * Executes whatever logic is needed for a specific node type.
 * This can be fetching data from an LMS, mapping data, publishing results, etc.
 */
fun interface NodeHandler {
	/**
	 * Process the current node, **not** its children.
	 */
	fun process(node: FlowNode, input: EdgeVertex): EdgeVertex
}

interface NodeRegistry {
	fun lookupHandler(descriptor: NodeDescriptor): NodeHandler?

	fun registerHandler(descriptor: NodeDescriptor, handler: NodeHandler)

	fun registerSubsystemHandler(subsystem: String, handler: NodeHandler)
}
