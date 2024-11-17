package me.snoty.backend.scheduling.jobrunr.node

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import me.snoty.backend.integration.flow.logging.FlowExecutionService
import me.snoty.backend.integration.flow.logging.NodeLogAppender
import me.snoty.backend.observability.APPENDER_LOG_LEVEL
import me.snoty.backend.observability.FLOW_ID
import me.snoty.backend.observability.JOB_ID
import me.snoty.backend.observability.USER_ID
import me.snoty.backend.scheduling.JobRequestHandler
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import me.snoty.integration.common.wiring.flow.FlowRunner
import me.snoty.integration.common.wiring.flow.FlowService
import org.jobrunr.jobs.context.JobRunrDashboardLogger
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import ch.qos.logback.classic.Logger as LogbackLogger

@Single
class JobRunrFlowJobHandler(
	private val flowService: FlowService,
	private val flowRunner: FlowRunner,
	flowExecutionService: FlowExecutionService,
) : JobRequestHandler<JobRunrFlowJobRequest> {
	private val rootLogger = LoggerFactory.getLogger(JobRunrFlowJobHandler::class.java) as LogbackLogger

	init {
		val nodeLogAppender = NodeLogAppender(flowExecutionService)
		nodeLogAppender.start()
		rootLogger.addAppender(nodeLogAppender)
	}

	override fun run(jobRequest: JobRunrFlowJobRequest) {
		val jobContext = jobContext()
		val logger = JobRunrDashboardLogger(this.rootLogger)

		MDC.put(JOB_ID.key, jobContext.jobId.toString())
		MDC.put(FLOW_ID.key, jobRequest.flowId.toString())
		MDC.put(APPENDER_LOG_LEVEL.key, jobRequest.logLevel.name)

		runBlocking(MDCContext()) {
			val flow = flowService.getWithNodes(jobRequest.flowId) ?: let {
				logger.error("Flow not found: {}", jobRequest.flowId)
				return@runBlocking
			}

			MDC.put(USER_ID.key, flow.userId.toString())

			logger.debug("Processing flow {}", flow)

			flowRunner.execute(
				jobId = jobContext.jobId.toString(),
				triggeredBy = jobRequest.triggeredBy,
				logger = logger,
				logLevel = jobRequest.logLevel,
				flow = flow,
				input = SimpleIntermediateData(jobContext),
			)
		}
	}
}
