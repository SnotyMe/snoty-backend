package me.snoty.backend.scheduling.jobrunr.node

import ch.qos.logback.classic.Level
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import me.snoty.backend.logging.KMDC
import me.snoty.backend.logging.NodeLogAppender
import me.snoty.backend.observability.*
import me.snoty.backend.scheduling.JobRequestHandler
import me.snoty.backend.wiring.flow.FlowRunner
import me.snoty.backend.wiring.flow.FlowService
import me.snoty.backend.wiring.flow.execution.FlowExecutionEventService
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import org.jobrunr.jobs.context.JobRunrDashboardLogger
import org.jobrunr.server.runner.ThreadLocalJobContext
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Logger as LogbackLogger

@Single
class JobRunrNodeJobHandler(
	private val flowService: FlowService,
	private val flowRunner: FlowRunner,
	flowExecutionService: FlowExecutionService,
	flowExecutionEventService: FlowExecutionEventService,
) : JobRequestHandler<JobRunrNodeJobRequest> {
	private val rootLogger = LoggerFactory.getLogger(JobRunrNodeJobHandler::class.java) as LogbackLogger

	init {
		val nodeLogAppender = NodeLogAppender(flowExecutionService, flowExecutionEventService)
		nodeLogAppender.start()
		rootLogger.addAppender(nodeLogAppender)
		// set to debug to allow filtering in the appender. Otherwise, the application log level would be inherited and block debug logs during node execution
		rootLogger.level = Level.DEBUG
	}

	override fun run(jobRequest: JobRunrNodeJobRequest) {
		val jobContext = ThreadLocalJobContext.getJobContext()
		val logger = JobRunrDashboardLogger(this.rootLogger)

		KMDC.put(FLOW_ID, jobRequest.flowId.value)
		KMDC.put(NODE_ID, jobRequest.nodeId.value)
		KMDC.put(JOB_ID, jobContext.jobId.toString())
		KMDC.put(APPENDER_LOG_LEVEL, jobRequest.logLevel.name)

		runBlocking(MDCContext()) {
			val flow = flowService.getWithNodes(userId = null, flowId = jobRequest.flowId) ?: let {
				logger.error("Flow not found: {}", jobRequest.flowId)
				return@runBlocking
			}

			KMDC.put(USER_ID, flow.userId.toString())

			val startNode = flow.nodes.singleOrNull { it.id == jobRequest.nodeId } ?: let {
				logger.error("Start Node not found: {} in Flow {}", jobRequest.nodeId, jobRequest.flowId)
				return@runBlocking
			}

			withContext(MDCContext()) {
				flowRunner.execute(
					jobId = jobContext.jobId.toString(),
					triggeredBy = jobRequest.triggeredBy,
					logger = logger,
					logLevel = jobRequest.logLevel,
					startNode = startNode,
					flow = flow,
					input = emptyList(),
				)
			}
		}
	}
}
