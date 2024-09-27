package me.snoty.integration.common.wiring.data

import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.get
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.NodeSettings

inline fun <reified T : Any> NodeHandleContext.each(input: Collection<IntermediateData>, block: (T) -> Unit): NodeOutput = input
	.forEach {
		block(get(it))
	}
	.let { emptyList() }

inline fun <reified T : Any, reified Settings : NodeSettings> NodeHandleContext.eachWithSettings(
	input: Collection<IntermediateData>,
	node: Node,
	block: (T, Settings) -> Unit
): NodeOutput {
	val settings = node.getConfig<Settings>()

	for (element in input) {
		val data = get<T>(element)
		block(data, settings)
	}

	return emptyList()
}

inline fun <reified T : Any> NodeHandleContext.mapInput(input: Collection<IntermediateData>, block: (T) -> NodeOutput): NodeOutput =
	input.flatMap {
		block(get<T>(it))
	}

inline fun <reified T : Any, reified Settings : NodeSettings> NodeHandleContext.mapInputWithSettings(
	input: Collection<IntermediateData>,
	node: Node,
	block: (T, Settings) -> NodeOutput
): NodeOutput {
	val settings = node.getConfig<Settings>()

	return input.flatMap {
		val data = get<T>(it)
		block(data, settings)
	}
}

inline fun <reified Settings : NodeSettings> Collection<IntermediateData>.mapWithSettings(node: Node, block: (Settings) -> NodeOutput): NodeOutput {
	val settings = node.getConfig<Settings>()

	return this.flatMap { block(settings) }
}
