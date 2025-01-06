package me.snoty.backend.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform

fun <T, C : Collection<T>> C.orNull() = ifEmpty { null }

fun <T, R> Flow<T>.listAsElements(block: suspend (T) -> Collection<R>): Flow<R> = this.transform { item ->
	val result = block(item)
	result.forEach {
		emit(it)
	}
}

/**
 * Executes the given block and returns its result or null if an exception occurred.
 */
fun <T, R> T.letOrNull(block: (T) -> R): R? = try {
	block(this)
} catch (e: Exception) {
	null
}

fun <T> flowOf(block: suspend () -> T): Flow<T> = flow {
	emit(block())
}

fun <T> flowOfEach(block: suspend () -> Collection<T>): Flow<T> = flow {
	block().forEach {
		emit(it)
	}
}
