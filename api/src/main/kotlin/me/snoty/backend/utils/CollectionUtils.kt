package me.snoty.backend.utils

fun <T> Collection<T>.filterIf(condition: Boolean, predicate: (T) -> Boolean): Collection<T> =
	if (condition) filter(predicate) else this

fun <T> Collection<T>.filterIfNot(condition: Boolean, predicate: (T) -> Boolean): Collection<T> =
	filterIf(!condition, predicate)
