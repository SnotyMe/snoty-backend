package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.wiring.EdgeVertex
import me.snoty.integration.common.wiring.IFlowNode

enum class NodePosition {
	START,
	MIDDLE,
	END
}

/**
 * Executes whatever logic is needed for ONE specific node type.
 *
 * This can be fetching data from an LMS, mapping data, publishing results, etc.
 *
 * One [NodeHandler] can only handle one type of node.
 */
interface NodeHandler {
	/**
	 * Process the current node, **not** its children.
	 */
	suspend fun process(node: IFlowNode, input: EdgeVertex): EdgeVertex

	/**
	 * Where the node is placed.
	 * Start nodes cannot have incoming edges.
	 * End nodes cannot have outgoing edges.
	 *
	 * Mostly useful to build a database query for all start nodes.
	 */
	val position: NodePosition
}

interface NodeRegistry {
	fun lookupHandler(descriptor: NodeDescriptor): NodeHandler?

	/**
	 * Highly specific function that returns all descriptors for nodes of a given position.
	 * This is useful for building a database query for all start nodes, for example.
	 */
	fun lookupDescriptorsByPosition(position: NodePosition): List<NodeDescriptor>

	fun registerHandler(descriptor: NodeDescriptor, handler: NodeHandler)

	fun getHandlers(): Map<NodeDescriptor, NodeHandler>
}

fun NodeRegistry.registerIntegrationHandler(type: String, handler: NodeHandler) {
	registerHandler(NodeDescriptor(Subsystem.INTEGRATION, type), handler)
}
