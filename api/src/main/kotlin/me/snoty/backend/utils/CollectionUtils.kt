package me.snoty.backend.utils

fun <T> Collection<T>.filterIf(condition: Boolean, predicate: (T) -> Boolean): Collection<T> =
	if (condition) filter(predicate) else this

fun <T> Collection<T>.filterIfNot(condition: Boolean, predicate: (T) -> Boolean): Collection<T> =
	filterIf(!condition, predicate)

fun <T> Collection<T>.skip(count: Int): Collection<T> = when {
	count < 0 -> error("Count must be non-negative")
	count >= size -> emptyList()
	else -> drop(count)
}
