package me.snoty.backend.integration.flow.execution

import io.github.oshai.kotlinlogging.slf4j.internal.Slf4jLogger
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.FlowNode
import org.slf4j.Logger
import org.slf4j.event.Level

data class FlowExecutionContext(
	val nodeMap: Map<NodeId, FlowNode>,
	val logger: Slf4jLogger<Logger>,
	val logLevel: Level,
	val flowTracing: FlowTracing,
) : FlowTracing by flowTracing
