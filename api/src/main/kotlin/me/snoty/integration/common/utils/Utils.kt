package me.snoty.integration.common.utils

fun <T : Any> guarded(value: Boolean, block: () -> T): Boolean =
	when (value) {
		true -> true
		false -> {
			block()
			false
		}
	}

fun <T> Collection<T>.filterNot(predicate: (T) -> Boolean, ifTrue: (T) -> Unit): List<T> = filter {
	when {
		!predicate(it) -> true
		else -> {
			ifTrue(it)
			false
		}
	}
}
