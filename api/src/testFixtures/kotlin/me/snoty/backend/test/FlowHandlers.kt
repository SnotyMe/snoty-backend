package me.snoty.backend.test

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.*
import me.snoty.integration.common.wiring.node.NodeHandler

const val TYPE_MAP = "map"
const val TYPE_QUOTE = "quote"
const val TYPE_EXCEPTION = "exception"
const val TYPE_WANTS_EMPTY_PROVIDES_NONEMPTY = "wants_empty_provides_nonempty"
const val TYPE_WANTS_NONEMPTY_PROVIDES_EMPTY = "wants_nonempty_provides_empty"

abstract class TestNodeHandler : NodeHandler

object NoOpNodeHandler : TestNodeHandler() {
	context(_: NodeHandleContext)
	override suspend fun process(node: Node, input: NodeInput) =
		iterableStructOutput(
			input.map { it.value }
		)
}

/**
 * A handler that quotes the input using single quotes.
 * `test` -> `'test'`
 */
object QuoteHandler : TestNodeHandler() {
	context(_: NodeHandleContext)
	override suspend fun process(node: Node, input: NodeInput) = mapInput<Any>(input) {
		simpleOutput("'${it}'")
	}
}

object ExceptionHandler : TestNodeHandler() {
	val exception = IllegalStateException("This is an exception")

	context(_: NodeHandleContext)
	override suspend fun process(node: Node, input: NodeInput)
		= throw exception
}

open class GlobalMapHandler(
	private val map: MutableMap<NodeId, NodeInput> = mutableMapOf()
) : TestNodeHandler(), Map<NodeId, NodeInput> by map {
	context(_: NodeHandleContext)
	override suspend fun process(node: Node, input: NodeInput) = processImpl(node, input)

	context(_: NodeHandleContext)
	fun processImpl(node: Node, input: NodeInput): NodeOutput {
		map[node._id] = input

		return mapInput<Any>(input) {
			simpleOutput(it)
		}
	}
}

class WantsEmptyProvidesNonEmptyHandler : GlobalMapHandler() {
	companion object {
		const val OUTPUT = "non-empty"
	}

	context(_: NodeHandleContext)
	override suspend fun process(node: Node, input: NodeInput): NodeOutput {
		super.processImpl(node, input)
		return simpleOutput(OUTPUT)
	}
}

class WantsNonEmptyProvidesEmptyHandler : GlobalMapHandler() {
	context(_: NodeHandleContext)
	override suspend fun process(node: Node, input: NodeInput) =
		if (input.isNotEmpty()) {
			super.processImpl(node, input)
			iterableStructOutput<Unit>(emptyList())
		}
		else error("Expected non-empty input, got: $input")
}
