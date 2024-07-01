package me.snoty.integration.common.fetch

import me.snoty.integration.common.wiring.node.NodeHandler
import org.jobrunr.jobs.context.JobContext

abstract class AbstractIntegrationFetcher : NodeHandler {
	fun logger(context: JobContext) = context.logger()
	fun progress(context: JobContext, stages: Long) = FetchContext(
		JobRunrFetchProgress(
			logger(context),
			context.progressBar(IntegrationProgressState.getStateCount(stages))
		),
	)
}
