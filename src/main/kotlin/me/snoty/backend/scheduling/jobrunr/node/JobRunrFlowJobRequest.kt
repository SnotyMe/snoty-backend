package me.snoty.backend.scheduling.jobrunr.node

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.JobRequest

data class JobRunrFlowJobRequest(
	val flowId: NodeId,
) : JobRequest {
	override fun getJobRequestHandler() = JobRunrFlowJobHandler::class.java
}
