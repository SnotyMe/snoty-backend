package me.snoty.backend.scheduling.impl.jobrunr.node

import kotlinx.coroutines.runBlocking
import me.snoty.backend.integration.flow.logging.FlowLogService
import me.snoty.backend.integration.flow.logging.NodeLogAppender
import me.snoty.backend.scheduling.JobRequestHandler
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.flow.FlowService
import org.jobrunr.jobs.context.JobRunrDashboardLogger
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Logger as LogbackLogger

@Single
class JobRunrFlowJobHandler(
	private val flowService: FlowService,
	private val flowRunner: FlowRunner,
	flowLogService: FlowLogService,
) : JobRequestHandler<JobRunrFlowJobRequest> {
	private val rootLogger = LoggerFactory.getLogger(JobRunrFlowJobHandler::class.java) as LogbackLogger

	init {
		val nodeLogAppender = NodeLogAppender(flowLogService)
		nodeLogAppender.start()
		rootLogger.addAppender(nodeLogAppender)
	}

	override fun run(jobRequest: JobRunrFlowJobRequest) {
		val jobContext = jobContext()
		val logger = JobRunrDashboardLogger(this.rootLogger)

		runBlocking {
			val flow = flowService.getWithNodes(jobRequest.flowId) ?: let {
				logger.error("Flow not found: {}", jobRequest.flowId)
				return@runBlocking
			}

			logger.debug("Processing flow {}", flow)

			flowRunner.execute(
				jobId = jobContext.jobId.toString(),
				logger = logger,
				flow = flow,
				input = SimpleIntermediateData(jobContext),
			)
		}
	}
}
