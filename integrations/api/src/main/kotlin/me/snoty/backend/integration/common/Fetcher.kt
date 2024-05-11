package me.snoty.backend.integration.common

import me.snoty.backend.integration.common.diff.EntityDiffMetrics
import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.JobRequestHandler
import me.snoty.backend.scheduling.Scheduler

interface Fetcher<S : JobRequest> : JobRequestHandler<S>

/**
 * Scheduler that delegates scheduling to the provided [scheduler] and prepends the [integrationName] to the job id.
 */
class SchedulerForIntegrations(private val integrationName: String, private val scheduler: Scheduler) {
	fun scheduleJob(idParts: Collection<Any>, job: JobRequest)
		// prepend `integrationName` to the `idParts` collection
		= scheduler.scheduleJob("$integrationName-${idParts.joinToString("-")}", job)
}

fun interface IntegrationFetcherFactory<S : JobRequest> {
	fun create(entityDiffMetrics: EntityDiffMetrics): Fetcher<S>
}
