package me.snoty.backend.scheduling.jobrunr.node

import kotlinx.serialization.Serializable
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.utils.Slf4jLevelSerializer
import me.snoty.core.FlowId
import org.slf4j.event.Level

@Serializable
data class JobRunrFlowJobRequest(
	val flowId: FlowId,
	val triggeredBy: FlowTriggerReason = FlowTriggerReason.Unknown,
	@Serializable(with = Slf4jLevelSerializer::class)
	val logLevel: Level = Level.INFO,
) : JobRequest {
	override fun getJobRequestHandler() = JobRunrFlowJobHandler::class.java
}
