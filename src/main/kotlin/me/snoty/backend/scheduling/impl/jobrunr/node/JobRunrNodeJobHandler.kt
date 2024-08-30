package me.snoty.backend.scheduling.impl.jobrunr.node

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import me.snoty.backend.integration.flow.logging.NodeLogAppender
import me.snoty.backend.integration.flow.logging.NodeLogService
import me.snoty.backend.scheduling.JobRequestHandler
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.jobrunr.jobs.context.JobRunrDashboardLogger
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Logger as LogbackLogger

@Single
class JobRunrNodeJobHandler(
	private val nodeRegistry: NodeRegistry,
	private val nodeService: NodeService,
	private val flowService: FlowService,
	nodeLogService: NodeLogService
) : JobRequestHandler<JobRunrNodeJobRequest> {
	private val rootLogger = LoggerFactory.getLogger(JobRunrNodeJobHandler::class.java) as LogbackLogger

	init {
		val nodeLogAppender = NodeLogAppender(nodeLogService)
		nodeLogAppender.start()
		rootLogger.addAppender(nodeLogAppender)
	}

	override fun run(jobRequest: JobRunrNodeJobRequest) {
		val jobContext = jobContext()
		val logger = JobRunrDashboardLogger(this.rootLogger)

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
			flowService.runFlow(jobContext.jobId.toString(), logger, node, SimpleIntermediateData(jobContext))
				.collect()
		}
	}
}
