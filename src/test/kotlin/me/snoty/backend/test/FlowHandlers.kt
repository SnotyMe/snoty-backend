package me.snoty.backend.test

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.data.mapInput
import me.snoty.integration.common.wiring.iterableStructOutput
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.simpleOutput
import org.slf4j.Logger

const val TYPE_MAP = "map"
const val TYPE_QUOTE = "quote"
const val TYPE_EXCEPTION = "exception"

abstract class TestNodeHandler : NodeHandler

object NoOpNodeHandler : TestNodeHandler() {
	context(NodeHandleContext)
	override suspend fun process(logger: Logger, node: Node, input: NodeInput) =
		iterableStructOutput(
			input.map { it.value }
		)
}

/**
 * A handler that quotes the input using single quotes.
 * `test` -> `'test'`
 */
object QuoteHandler : TestNodeHandler() {
	context(NodeHandleContext)
	override suspend fun process(logger: Logger, node: Node, input: NodeInput) = input.mapInput<Any> {
		simpleOutput("'${it}'")
	}
}

object ExceptionHandler : TestNodeHandler() {
	val exception = IllegalStateException("This is an exception")

	context(NodeHandleContext)
	override suspend fun process(logger: Logger, node: Node, input: NodeInput)
		= throw exception
}

class GlobalMapHandler(
	private val map: MutableMap<NodeId, Any> = mutableMapOf()
) : TestNodeHandler(), Map<NodeId, Any> by map {
	context(NodeHandleContext)
	override suspend fun process(logger: Logger, node: Node, input: NodeInput) = input.mapInput<Any> {
		map[node._id] = it

		simpleOutput(it)
	}
}
