package me.snoty.backend.test

import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.node.NodeHandler
import kotlin.random.Random

private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun randomString(stringLength: Int = 32) =
	(1..stringLength)
		.map { Random.nextInt(0, charPool.size).let { charPool[it] } }
		.joinToString("")

fun getClassNameFromBlock(block: Function<*>): String {
	val javaClass = block.javaClass
	val name = javaClass.name
	return when {
		name.contains("Kt$") -> name.substringBefore("Kt$")
		name.contains("$") -> name.substringBefore("$")
		else -> name
	}.substringAfterLast(".")
}

context(context: NodeHandleContext, handler: NodeHandler)
suspend fun process(node: Node, input: NodeInput): NodeOutput = with (context) {
	with (handler) {
		return process(node, input)
	}
}
