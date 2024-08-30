package me.snoty.backend.scheduling.impl.jobrunr.node

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.JobRequest

data class JobRunrNodeJobRequest(
	val nodeId: NodeId
) : JobRequest {
	override fun getJobRequestHandler() = JobRunrNodeJobHandler::class.java
}
