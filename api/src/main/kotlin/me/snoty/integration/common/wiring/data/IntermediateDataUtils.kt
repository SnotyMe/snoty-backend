package me.snoty.integration.common.wiring.data

import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.NodeSettings

context(ctx: NodeHandleContext)
inline fun <reified T : Any> each(input: Collection<IntermediateData>, block: (T) -> Unit): NodeOutput = input
	.forEach {
		block(it.get())
	}
	.let { emptyList() }

context(ctx: NodeHandleContext)
inline fun <reified T : Any, reified Settings : NodeSettings> eachWithSettings(
	input: Collection<IntermediateData>,
	node: Node,
	block: (T, Settings) -> Unit
): NodeOutput {
	val settings = node.getConfig<Settings>()

	for (element in input) {
		val data = element.get<T>()
		block(data, settings)
	}

	return emptyList()
}

context(ctx: NodeHandleContext)
inline fun <reified T : Any> mapInput(input: Collection<IntermediateData>, block: (T) -> NodeOutput): NodeOutput =
	input.flatMap {
		block(it.get<T>())
	}

context(ctx: NodeHandleContext)
inline fun <reified T : Any, reified Settings : NodeSettings> mapInputWithSettings(
	input: Collection<IntermediateData>,
	node: Node,
	block: (T, Settings) -> NodeOutput
): NodeOutput {
	val settings = node.getConfig<Settings>()

	return input.flatMap {
		val data = it.get<T>()
		block(data, settings)
	}
}

inline fun <reified Settings : NodeSettings> Collection<IntermediateData>.mapWithSettings(node: Node, block: (Settings) -> NodeOutput): NodeOutput {
	val settings = node.getConfig<Settings>()

	return this.flatMap { block(settings) }
}
