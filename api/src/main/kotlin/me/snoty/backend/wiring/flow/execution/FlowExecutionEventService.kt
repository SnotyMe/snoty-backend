package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.NodeLogEntry

@Serializable
sealed class FlowExecutionEvent(val eventType: String) {
	abstract val userId: String
	abstract val flowId: NodeId
	val timestamp = Clock.System.now()
	
	@Serializable
	data class FlowStartedEvent(
		override val userId: String,
		override val flowId: String,
		val jobId: String,
		val triggeredBy: FlowTriggerReason,
	) : FlowExecutionEvent("FlowStarted")
	
	@Serializable
	data class FlowLogEvent(
		override val userId: String,
		override val flowId: String,
		val jobId: String,
		val entry: NodeLogEntry,
	) : FlowExecutionEvent("FlowLog")

	@Serializable
	data class FlowEndedEvent(
		override val userId: String,
		override val flowId: String,
		val jobId: String,
		val status: FlowExecutionStatus,
	) : FlowExecutionEvent("FlowEnded")
}

interface FlowExecutionEventService {
	fun provideBus(): SharedFlow<FlowExecutionEvent>

	/**
	 * Offers an event. Will be passed on using the database or in-memory channel.
	 */
	suspend fun offer(event: FlowExecutionEvent)
}
