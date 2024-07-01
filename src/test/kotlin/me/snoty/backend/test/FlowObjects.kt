package me.snoty.backend.test

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.EdgeVertex
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeSettings
import kotlin.reflect.KClass

object NoOpNodeHandler : NodeHandler {
	override val position = NodePosition.END
	override val settingsClass: KClass<out NodeSettings> = NodeSettings::class
	override suspend fun process(node: IFlowNode, input: EdgeVertex): EdgeVertex {
		return input
	}
}

/**
 * A handler that quotes the input using single quotes.
 * `test` -> `'test'`
 */
object QuoteHandler : NodeHandler {
	override val position = NodePosition.MIDDLE
	override val settingsClass: KClass<out NodeSettings> = NodeSettings::class
	override suspend fun process(node: IFlowNode, input: EdgeVertex): EdgeVertex {
		return "'$input'"
	}
}

class GlobalMapHandler(
	private val map: MutableMap<NodeId, Any> = mutableMapOf()
) : NodeHandler, Map<NodeId, Any> by map {
	override val position = NodePosition.END
	override val settingsClass: KClass<out NodeSettings> = NodeSettings::class
	override suspend fun process(node: IFlowNode, input: EdgeVertex): EdgeVertex {
		map[node._id] = input
		return input
	}
}
