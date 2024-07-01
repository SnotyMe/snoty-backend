package me.snoty.integration.common.fetch

import me.snoty.integration.common.wiring.node.NodeHandler
import org.jobrunr.jobs.context.JobContext
import org.jobrunr.jobs.context.JobRunrDashboardLogger
import org.slf4j.LoggerFactory

abstract class AbstractIntegrationFetcher : NodeHandler {
	protected val logger = JobRunrDashboardLogger(LoggerFactory.getLogger(this::class.java))

	fun logger(context: JobContext) = context.logger()
	fun progress(context: JobContext, stages: Long) = FetchContext(
		JobRunrFetchProgress(
			logger(context),
			context.progressBar(IntegrationProgressState.getStateCount(stages))
		),
	)
}
