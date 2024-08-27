package me.snoty.backend.test

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.Subsystem
import me.snoty.integration.common.wiring.simpleOutput
import org.slf4j.Logger

const val TYPE_MAP = "map"
const val TYPE_QUOTE = "quote"
const val TYPE_EXCEPTION = "exception"

abstract class TestNodeHandler : NodeHandler

object NoOpNodeHandler : TestNodeHandler() {
	override val metadata = nodeMetadata(NodeDescriptor(Subsystem.PROCESSOR, "noop"))

	context(NodeHandleContext)
	override suspend fun process(logger: Logger, node: Node, input: IntermediateData) {
		simpleOutput {
			input.value
		}
	}
}

/**
 * A handler that quotes the input using single quotes.
 * `test` -> `'test'`
 */
object QuoteHandler : TestNodeHandler() {
	override val metadata = nodeMetadata(NodeDescriptor(Subsystem.PROCESSOR, TYPE_QUOTE))

	context(NodeHandleContext)
	override suspend fun process(logger: Logger, node: Node, input: IntermediateData) {
		simpleOutput {
			"'${input.value}'"
		}
	}
}

object ExceptionHandler : TestNodeHandler() {
	override val metadata = nodeMetadata(NodeDescriptor(Subsystem.PROCESSOR, TYPE_EXCEPTION))
	val exception = IllegalStateException("This is an exception")

	context(NodeHandleContext)
	override suspend fun process(logger: Logger, node: Node, input: IntermediateData) {
		throw exception
	}
}

class GlobalMapHandler(
	private val map: MutableMap<NodeId, Any> = mutableMapOf()
) : TestNodeHandler(), Map<NodeId, Any> by map {
	override val metadata = nodeMetadata(NodeDescriptor(Subsystem.PROCESSOR, TYPE_MAP))

	context(NodeHandleContext)
	override suspend fun process(logger: Logger, node: Node, input: IntermediateData) {
		map[node._id] = input.value
		simpleOutput {
			input.value
		}
	}
}
