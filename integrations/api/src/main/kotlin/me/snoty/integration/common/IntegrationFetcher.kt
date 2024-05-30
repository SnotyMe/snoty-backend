package me.snoty.integration.common

import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.JobRequestHandler
import me.snoty.integration.common.diff.EntityStateService

interface IntegrationFetcher<S : JobRequest> : JobRequestHandler<S>

fun interface IntegrationFetcherFactory<S : JobRequest, ID : Comparable<ID>> {
	fun create(
		entityStateService: EntityStateService
	): IntegrationFetcher<S>
}
