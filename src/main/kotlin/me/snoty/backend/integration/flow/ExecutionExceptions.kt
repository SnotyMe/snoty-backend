package me.snoty.backend.integration.flow

/**
 * A single node has failed to execute.
 */
class NodeExecutionException(wrapped: Throwable) : Exception(wrapped)

/**
 * An entire flow has failed to execute.
 */
class FlowExecutionException(nodeException: Throwable) : Exception(nodeException)
