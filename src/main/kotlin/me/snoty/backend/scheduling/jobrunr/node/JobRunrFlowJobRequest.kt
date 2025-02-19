package me.snoty.backend.scheduling.jobrunr.node

import kotlinx.serialization.Serializable
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.scheduling.JobRequest
import org.slf4j.event.Level

@Serializable
data class JobRunrFlowJobRequest(
	val flowId: NodeId,
	val triggeredBy: FlowTriggerReason = FlowTriggerReason.Unknown,
	val logLevel: Level = Level.INFO,
) : JobRequest {
	override fun getJobRequestHandler() = JobRunrFlowJobHandler::class.java
}
