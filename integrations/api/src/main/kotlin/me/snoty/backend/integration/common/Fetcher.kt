package me.snoty.backend.integration.common

import me.snoty.backend.integration.common.diff.EntityDiffMetrics
import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.JobRequestHandler

interface Fetcher<S : JobRequest> : JobRequestHandler<S>

fun interface IntegrationFetcherFactory<S : JobRequest> {
	fun create(entityDiffMetrics: EntityDiffMetrics): Fetcher<S>
}
