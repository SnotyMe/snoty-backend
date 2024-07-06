package me.snoty.backend.integration.flow

import io.opentelemetry.api.trace.Span
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import me.snoty.backend.observability.setException
import me.snoty.integration.common.wiring.RelationalFlowNode

fun <T> Flow<T>.flowCatching(span: Span) = catch {
	// exception has already been handled
	// we rethrow to handle it again at the root
	if (it is NodeExecutionException) throw it
	span.setException(it)
	throw NodeExecutionException(it)
}

fun traceName(node: RelationalFlowNode) =
	"Node ${node.descriptor.subsystem}:${node.descriptor.type} (${node._id})"
