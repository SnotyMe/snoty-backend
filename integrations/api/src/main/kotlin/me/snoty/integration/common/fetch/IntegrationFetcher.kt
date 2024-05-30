package me.snoty.integration.common.fetch

import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.JobRequestHandler
import me.snoty.integration.common.diff.EntityStateService
import org.jobrunr.jobs.context.JobContext

interface IntegrationFetcher<S : JobRequest> : JobRequestHandler<S>

abstract class AbstractIntegrationFetcher<R : JobRequest> : IntegrationFetcher<R> {
	fun logger(context: JobContext) = context.logger()
	fun progress(context: JobContext, stages: Long) =
		JobRunrFetchProgress(logger(context), context.progressBar(IntegrationProgressState.getStateCount(stages)))
}

fun interface IntegrationFetcherFactory<S : JobRequest, ID : Comparable<ID>> {
	fun create(
		entityStateService: EntityStateService
	): IntegrationFetcher<S>
}
