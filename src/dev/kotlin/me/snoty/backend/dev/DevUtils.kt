package me.snoty.backend.dev

import kotlin.random.Random

private val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun randomString(stringLength: Int = 32) =
	(1..stringLength)
	.map { Random.nextInt(0, charPool.size).let { charPool[it] } }
	.joinToString("")
