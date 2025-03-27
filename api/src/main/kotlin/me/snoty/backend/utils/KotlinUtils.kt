package me.snoty.backend.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Executes the given block and returns its result or null if an exception occurred.
 */
fun <T, R> T.letOrNull(block: (T) -> R): R? = try {
	block(this)
} catch (_: Exception) {
	null
}

fun <T> flowOfEach(block: suspend () -> Collection<T>): Flow<T> = flow {
	block().forEach {
		emit(it)
	}
}
