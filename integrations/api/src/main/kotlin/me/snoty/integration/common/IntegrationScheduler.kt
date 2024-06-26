package me.snoty.integration.common

import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.scheduling.SnotyJob

/**
 * Scheduler that delegates scheduling to the provided [scheduler] and prepends the [integrationName] to the job id.
 */
class IntegrationScheduler(private val integrationName: String, private val scheduler: Scheduler) {
	fun scheduleJob(idParts: Collection<Any>, job: SnotyJob)
		// create a job called "integrationName-instanceId-userId"
		= scheduler.scheduleJob("$integrationName-${idParts.joinToString("-")}", job)
}
