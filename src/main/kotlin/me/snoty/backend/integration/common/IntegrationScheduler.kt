package me.snoty.backend.integration.common

import me.snoty.backend.integration.common.diff.EntityDiffMetrics

fun interface IntegrationScheduler<S> {
	fun schedule(config: IntegrationConfig<S>)
}

fun interface IntegrationSchedulerFactory<S> {
	fun create(entityDiffMetrics: EntityDiffMetrics): IntegrationScheduler<S>
}
