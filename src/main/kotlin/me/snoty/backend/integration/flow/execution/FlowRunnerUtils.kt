package me.snoty.backend.integration.flow.execution

import io.opentelemetry.api.trace.Span
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import me.snoty.backend.integration.flow.NodeExecutionException
import me.snoty.backend.observability.setException

fun <T> Flow<T>.flowCatching(span: Span) = catch {
	// exception has already been handled
	// we rethrow to handle it again at the root
	if (it is NodeExecutionException) throw it
	span.setException(it)
	throw NodeExecutionException(it)
}
