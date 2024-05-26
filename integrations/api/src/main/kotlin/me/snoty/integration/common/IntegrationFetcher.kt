package me.snoty.integration.common

import com.mongodb.kotlin.client.coroutine.MongoCollection
import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.JobRequestHandler
import me.snoty.integration.common.diff.EntityDiffMetrics
import me.snoty.integration.common.diff.UserEntityChanges
import me.snoty.integration.common.diff.state.UserEntityStates

interface IntegrationFetcher<S : JobRequest> : JobRequestHandler<S>

fun interface IntegrationFetcherFactory<S : JobRequest, ID : Comparable<ID>> {
	fun create(
		entityDiffMetrics: EntityDiffMetrics,
		stateCollection: MongoCollection<UserEntityStates>,
		changesCollection: MongoCollection<UserEntityChanges>
	): IntegrationFetcher<S>
}
