package me.snoty.integration.common

import me.snoty.integration.common.diff.EntityDiffMetrics
import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.JobRequestHandler

interface IntegrationFetcher<S : JobRequest> : JobRequestHandler<S>

fun interface IntegrationFetcherFactory<S : JobRequest> {
	fun create(entityDiffMetrics: EntityDiffMetrics): IntegrationFetcher<S>
}
