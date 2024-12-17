package me.snoty.backend.utils.errors

class CaughtException(override val cause: Throwable) : RuntimeException(cause)

fun Throwable.nullIfCaught(): Throwable? = when (this) {
	is CaughtException -> null
	else -> this
}

fun Throwable.causeIfCaught(): Throwable = when (this) {
	is CaughtException -> cause
	else -> this
}
