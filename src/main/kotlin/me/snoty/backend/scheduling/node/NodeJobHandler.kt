package me.snoty.backend.scheduling.node

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import me.snoty.backend.scheduling.JobRequestHandler
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.jobrunr.jobs.context.JobRunrDashboardLogger
import org.slf4j.LoggerFactory

class NodeJobHandler(
	private val nodeRegistry: NodeRegistry,
	private val nodeService: NodeService,
	private val flowService: FlowService
) : JobRequestHandler<NodeJobRequest> {
	override fun run(jobRequest: NodeJobRequest) {
		val jobContext = jobContext()
		// TODO: consistent logger name with AbstractIntegrationFetcher
		val logger = JobRunrDashboardLogger(LoggerFactory.getLogger(this::class.java))
		runBlocking {
			val node = nodeService.get(jobRequest.nodeId)

			if (node == null) {
				// TODO: potentially delete the job?
				logger.warn("Node with id ${jobRequest.nodeId} not found")
				return@runBlocking
			}

			val handler = nodeRegistry.lookupHandler(node.descriptor)
			if (handler == null) {
				logger.warn("No handler found for node ${node.descriptor}")
				return@runBlocking
			}

			logger.debug("Processing flow for node {}", node.descriptor)
			flowService.runFlow(jobContext.jobId.toString(), logger, node, jobContext)
				.collect()
		}
	}
}
