package me.snoty.backend.test

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.data.mapInput
import me.snoty.integration.common.wiring.iterableStructOutput
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.simpleOutput

const val TYPE_MAP = "map"
const val TYPE_QUOTE = "quote"
const val TYPE_EXCEPTION = "exception"
const val TYPE_WANTS_EMPTY_PROVIDES_NONEMPTY = "wants_empty_provides_nonempty"
const val TYPE_WANTS_NONEMPTY_PROVIDES_EMPTY = "wants_nonempty_provides_empty"

abstract class TestNodeHandler : NodeHandler

object NoOpNodeHandler : TestNodeHandler() {
	override suspend fun NodeHandleContext.process(node: Node, input: NodeInput) =
		iterableStructOutput(
			input.map { it.value }
		)
}

/**
 * A handler that quotes the input using single quotes.
 * `test` -> `'test'`
 */
object QuoteHandler : TestNodeHandler() {
	override suspend fun NodeHandleContext.process(node: Node, input: NodeInput) = mapInput<Any>(input) {
		simpleOutput("'${it}'")
	}
}

object ExceptionHandler : TestNodeHandler() {
	val exception = IllegalStateException("This is an exception")

	override suspend fun NodeHandleContext.process(node: Node, input: NodeInput)
		= throw exception
}

open class GlobalMapHandler(
	private val map: MutableMap<NodeId, NodeInput> = mutableMapOf()
) : TestNodeHandler(), Map<NodeId, NodeInput> by map {
	override suspend fun NodeHandleContext.process(node: Node, input: NodeInput) = processImpl(this, node, input)
	
	fun processImpl(nodeHandleContext: NodeHandleContext, node: Node, input: NodeInput): NodeOutput {
		map[node._id] = input

		return nodeHandleContext.mapInput<Any>(input) {
			nodeHandleContext.simpleOutput(it)
		}
	}
}

class WantsEmptyProvidesNonEmptyHandler : GlobalMapHandler() {
	companion object {
		const val OUTPUT = "non-empty"
	}
	
	override suspend fun NodeHandleContext.process(node: Node, input: NodeInput): NodeOutput {
		super.processImpl(this, node, input)
		return simpleOutput(OUTPUT)
	}
}

class WantsNonEmptyProvidesEmptyHandler : GlobalMapHandler() {
	override suspend fun NodeHandleContext.process(node: Node, input: NodeInput) =
		if (input.isNotEmpty()) {
			super.processImpl(this, node, input)
			iterableStructOutput<Unit>(emptyList())
		}
		else error("Expected non-empty input, got: $input")
}
