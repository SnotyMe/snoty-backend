package me.snoty.backend.test

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.NodeHandlerContext
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.simpleOutput
import org.slf4j.Logger

const val TYPE_MAP = "map"
const val TYPE_QUOTE = "quote"
const val TYPE_EXCEPTION = "exception"

abstract class TestNodeHandler : NodeHandler {
	override val settingsClass = NodeSettings::class
	override val nodeHandlerContext = MockNodeHandlerContext
}

object NoOpNodeHandler : TestNodeHandler() {
	override val position = NodePosition.END

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: IFlowNode, input: IntermediateData) {
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
	override val position = NodePosition.MIDDLE

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: IFlowNode, input: IntermediateData) {
		simpleOutput {
			"'${input.value}'"
		}
	}
}

object ExceptionHandler : TestNodeHandler() {
	override val position = NodePosition.END

	val exception = IllegalStateException("This is an exception")

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: IFlowNode, input: IntermediateData) {
		throw exception
	}
}

class GlobalMapHandler(
	private val map: MutableMap<NodeId, Any> = mutableMapOf()
) : TestNodeHandler(), Map<NodeId, Any> by map {
	override val position = NodePosition.END

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: IFlowNode, input: IntermediateData) {
		map[node._id] = input.value
		simpleOutput {
			input.value
		}
	}
}
