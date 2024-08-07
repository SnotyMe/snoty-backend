package me.snoty.integration.common.fetch

import org.jobrunr.jobs.context.JobContext
import org.slf4j.Logger

class FetchContext(
	val logger: Logger,
	private val progress: FetchProgress,
) : FetchProgress by progress {
	suspend fun <T> fetchStage(block: suspend () -> T): T {
		progress.advance(IntegrationProgressState.FETCHING)
		return block()
	}

	suspend fun updateStage(block: suspend () -> Unit) {
		progress.advance(IntegrationProgressState.UPDATING_IN_DB)
		block()
	}
}

fun fetchContext(logger: Logger, context: JobContext, stages: Long) = FetchContext(
	logger,
	JobRunrFetchProgress(
		logger,
		context.progressBar(IntegrationProgressState.getStateCount(stages))
	),
)
