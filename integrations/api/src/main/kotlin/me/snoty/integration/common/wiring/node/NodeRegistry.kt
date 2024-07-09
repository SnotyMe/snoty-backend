package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.wiring.NodeContextBuilder
import me.snoty.integration.common.wiring.NodeHandlerContext

enum class NodePosition {
	START,
	MIDDLE,
	END
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

fun NodeRegistry.registerHandler(
	subsystem: String,
	type: String,
	nodeContextBuilder: NodeContextBuilder,
	handlerBuilder: (NodeHandlerContext) -> NodeHandler
): NodeDescriptor {
	val descriptor = NodeDescriptor(subsystem, type)
	val nodeContext = nodeContextBuilder(descriptor)
	registerHandler(descriptor, handlerBuilder(nodeContext))
	return descriptor
}

fun NodeRegistry.registerIntegrationHandler(
	type: String,
	nodeContextBuilder: NodeContextBuilder,
	handlerBuilder: (NodeHandlerContext) -> NodeHandler
): NodeDescriptor =
	registerHandler(Subsystem.INTEGRATION, type, nodeContextBuilder, handlerBuilder)
