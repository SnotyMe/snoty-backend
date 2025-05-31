package me.snoty.backend.integration.flow

import me.snoty.integration.common.wiring.GenericNode

/**
 * A single node has failed to execute.
 */
class NodeExecutionException(val node: GenericNode, wrapped: Throwable) : Exception(wrapped)

/**
 * An entire flow has failed to execute.
 */
class FlowExecutionException(nodeException: Throwable) : Exception(nodeException)

fun Throwable.unwrap(): Throwable = when (this) {
	is FlowExecutionException -> cause?.unwrap() ?: this
	is NodeExecutionException -> cause?.unwrap() ?: this
	else -> this
}

fun Throwable.unwrapNodeException(): NodeExecutionException? = when (this) {
	is FlowExecutionException -> cause?.unwrapNodeException()
	is NodeExecutionException -> this
	else -> null
}
