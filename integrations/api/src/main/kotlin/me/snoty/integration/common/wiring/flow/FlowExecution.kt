package me.snoty.integration.common.wiring.flow

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import me.snoty.backend.integration.config.flow.NodeId

@Serializable
data class FlowExecution(
	val jobId: String,
	val flowId: NodeId,
	val startDate: Instant,
	val status: FlowExecutionStatus?,
)

enum class FlowExecutionStatus {
	RUNNING,
	SUCCESS,
	FAILED,
}
