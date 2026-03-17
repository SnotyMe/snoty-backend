package me.snoty.integration.common.wiring.flow

import kotlinx.serialization.Serializable
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.core.FlowId
import kotlin.time.Instant

@Serializable
data class EnumeratedFlowExecution(
	val jobId: String,
	val flowId: FlowId,
	val triggeredBy: FlowTriggerReason,
	val timestamp: Instant,
	val status: FlowExecutionStatus,
)

@Serializable
data class FlowExecution(
	val jobId: String,
	val flowId: FlowId,
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
