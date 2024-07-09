package me.snoty.backend.test

import io.mockk.mockk
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.NodeHandlerContext
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.structOutput
import org.slf4j.Logger
import kotlin.reflect.KClass

const val TYPE_MAP = "map"
const val TYPE_QUOTE = "quote"
const val TYPE_EXCEPTION = "exception"

object NoOpNodeHandler : NodeHandler {
	override val position = NodePosition.END
	override val settingsClass: KClass<out NodeSettings> = NodeSettings::class
	override val nodeHandlerContext: NodeHandlerContext = mockk()

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: IFlowNode, input: IntermediateData) {
		structOutput {
			input.value
		}
	}
}

/**
 * A handler that quotes the input using single quotes.
 * `test` -> `'test'`
 */
object QuoteHandler : NodeHandler {
	override val position = NodePosition.MIDDLE
	override val settingsClass: KClass<out NodeSettings> = NodeSettings::class
	override val nodeHandlerContext: NodeHandlerContext = mockk()

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: IFlowNode, input: IntermediateData) {
		structOutput {
			"'${input.value}'"
		}
	}
}

object ExceptionHandler : NodeHandler {
	override val position = NodePosition.END
	override val settingsClass: KClass<out NodeSettings> = NodeSettings::class
	override val nodeHandlerContext: NodeHandlerContext = mockk()

	val exception = IllegalStateException("This is an exception")

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: IFlowNode, input: IntermediateData) {
		throw exception
	}
}

class GlobalMapHandler(
	private val map: MutableMap<NodeId, Any> = mutableMapOf()
) : NodeHandler, Map<NodeId, Any> by map {
	override val position = NodePosition.END
	override val settingsClass: KClass<out NodeSettings> = NodeSettings::class
	override val nodeHandlerContext: NodeHandlerContext = mockk()

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: IFlowNode, input: IntermediateData) {
		map[node._id] = input.value
		structOutput {
			input.value
		}
	}
}
