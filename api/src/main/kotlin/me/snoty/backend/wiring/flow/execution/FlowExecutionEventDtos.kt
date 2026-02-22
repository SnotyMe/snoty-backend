package me.snoty.backend.wiring.flow.execution

import kotlinx.serialization.Serializable
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.core.FlowId
import me.snoty.core.UserId
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.NodeLogEntry
import kotlin.time.Clock

@Serializable
sealed class FlowExecutionEvent(val eventType: String) {
	abstract val userId: UserId
	abstract val flowId: FlowId
	val timestamp = Clock.System.now()
	
	@Serializable
	data class FlowStartedEvent(
		override val userId: UserId,
		override val flowId: FlowId,
		val jobId: String,
		val triggeredBy: FlowTriggerReason,
	) : FlowExecutionEvent("FlowStarted")
	
	@Serializable
	data class FlowLogEvent(
		override val userId: UserId,
		override val flowId: FlowId,
		val jobId: String,
		val entry: NodeLogEntry,
	) : FlowExecutionEvent("FlowLog")

	@Serializable
	data class FlowEndedEvent(
		override val userId: UserId,
		override val flowId: FlowId,
		val jobId: String,
		val status: FlowExecutionStatus,
	) : FlowExecutionEvent("FlowEnded")
}
