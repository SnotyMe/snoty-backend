package me.snoty.integration.common.utils

import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.SnotyJob
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.node.NodeDescriptor

fun createFetcherJob(descriptor: NodeDescriptor, node: Node, request: JobRequest): SnotyJob {
	val user = node.userId
	val integrationName = descriptor.type
	return SnotyJob(
		name = "[${node._id}] rootIntegration=$integrationName user=$user",
		request
	)
}
