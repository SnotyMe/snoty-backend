package me.snoty.backend.scheduling.jobrunr.node

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.JobRequest
import org.slf4j.event.Level

data class JobRunrFlowJobRequest(
	val flowId: NodeId,
	val logLevel: Level = Level.INFO,
) : JobRequest {
	override fun getJobRequestHandler() = JobRunrFlowJobHandler::class.java
}
