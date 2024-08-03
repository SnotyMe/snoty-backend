package me.snoty.integration.common.fetch

import me.snoty.integration.common.wiring.node.NodeHandler
import org.jobrunr.jobs.context.JobContext
import org.slf4j.Logger

abstract class AbstractIntegrationFetcher : NodeHandler {
	fun progress(logger: Logger, context: JobContext, stages: Long) = FetchContext(
		logger,
		JobRunrFetchProgress(
			logger,
			context.progressBar(IntegrationProgressState.getStateCount(stages))
		),
	)
}
