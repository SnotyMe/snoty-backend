package me.snoty.backend.test

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.EdgeVertex
import me.snoty.backend.integration.flow.model.FlowNode
import me.snoty.backend.integration.flow.node.NodeHandler

object NoOpNodeHandler : NodeHandler {
	override fun process(node: FlowNode, input: EdgeVertex): EdgeVertex {
		return input
	}
}

/**
 * A handler that quotes the input using single quotes.
 * `test` -> `'test'`
 */
object QuoteHandler : NodeHandler {
	override fun process(node: FlowNode, input: EdgeVertex): EdgeVertex {
		return "'$input'"
	}
}

class GlobalMapHandler(
	private val map: MutableMap<NodeId, Any> = mutableMapOf()
) : NodeHandler, Map<NodeId, Any> by map {
	override fun process(node: FlowNode, input: EdgeVertex): EdgeVertex {
		map[node.id] = input
		return input
	}
}
