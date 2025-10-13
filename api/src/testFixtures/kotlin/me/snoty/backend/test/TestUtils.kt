package me.snoty.backend.test

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
