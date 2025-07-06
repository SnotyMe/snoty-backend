package me.snoty.integration.common.wiring.flow

import kotlinx.serialization.Serializable
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.FlowTriggerReason
import kotlin.time.Instant

@Serializable
data class EnumeratedFlowExecution(
	val jobId: String,
	val flowId: NodeId,
	val triggeredBy: FlowTriggerReason,
	val startDate: Instant,
	val status: FlowExecutionStatus?,
)

@Serializable
data class FlowExecution(
	val jobId: String,
	val flowId: NodeId,
	val triggeredBy: FlowTriggerReason,
	val startDate: Instant,
	val status: FlowExecutionStatus?,
	val logs: List<NodeLogEntry>?,
)

enum class FlowExecutionStatus {
	RUNNING,
	SUCCESS,
	FAILED,
}
