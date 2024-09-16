package me.snoty.integration.common.wiring.data

import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.get
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.NodeSettings

context(NodeHandleContext)
inline fun <reified T : Any> Collection<IntermediateData>.each(block: (T) -> Unit): NodeOutput =
	forEach {
		block(it.get())
	}.let { emptyList() }

context(NodeHandleContext)
inline fun <reified T : Any, reified Settings : NodeSettings> Collection<IntermediateData>.eachWithSettings(node: Node, block: (T, Settings) -> Unit): NodeOutput {
	val settings = node.getConfig<Settings>()

	for (element in this) {
		val data = element.get<T>()
		block(data, settings)
	}

	return emptyList()
}

context(NodeHandleContext)
inline fun <reified T : Any> Collection<IntermediateData>.mapInput(block: (T) -> NodeOutput): NodeOutput =
	this.flatMap {
		block(it.get<T>())
	}

context(NodeHandleContext)
inline fun <reified T : Any, reified Settings : NodeSettings> mapInputWithSettings(node: Node, input: Collection<IntermediateData>, block: (T, Settings) -> NodeOutput): NodeOutput {
	val settings = node.getConfig<Settings>()

	return input.flatMap {
		val data = it.get<T>()
		block(data, settings)
	}
}

context(NodeHandleContext)
inline fun <reified Settings : NodeSettings> Collection<IntermediateData>.mapWithSettings(node: Node, block: (Settings) -> NodeOutput): NodeOutput {
	val settings = node.getConfig<Settings>()

	return this.flatMap { block(settings) }
}
