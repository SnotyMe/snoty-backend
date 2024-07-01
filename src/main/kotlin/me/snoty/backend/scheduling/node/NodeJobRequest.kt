package me.snoty.backend.scheduling.node

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.JobRequest

data class NodeJobRequest(
	val nodeId: NodeId
) : JobRequest {
	override fun getJobRequestHandler() = NodeJobHandler::class.java
}
