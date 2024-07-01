package me.snoty.integration.common.utils

import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.SnotyJob
import me.snoty.integration.common.wiring.StandaloneFlowNode
import me.snoty.integration.common.wiring.node.NodeDescriptor

fun createFetcherJob(descriptor: NodeDescriptor, node: StandaloneFlowNode, request: JobRequest): SnotyJob {
	val user = node.userId
	// TODO: format settings
	val settingsString = node.config
	val integrationName = descriptor.type
	return SnotyJob(
		name = "[fetcher] <$integrationName> user=$user settings=($settingsString)",
		request
	)
}
