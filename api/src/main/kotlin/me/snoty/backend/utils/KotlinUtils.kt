package me.snoty.backend.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

fun <T, C : Collection<T>> C.orNull() = ifEmpty { null }

inline fun <T> T.contextual(block: T.() -> Unit) {
	block()
}

fun <T, R> Flow<T>.listAsElements(block: suspend (T) -> Collection<R>): Flow<R> = this.transform { item ->
	val result = block(item)
	result.forEach {
		emit(it)
	}
}
