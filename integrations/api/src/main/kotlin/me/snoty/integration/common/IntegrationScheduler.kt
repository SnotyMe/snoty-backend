package me.snoty.integration.common

import me.snoty.backend.scheduling.JobRequest
import me.snoty.backend.scheduling.Scheduler

/**
 * Scheduler that delegates scheduling to the provided [scheduler] and prepends the [integrationName] to the job id.
 */
class IntegrationScheduler(private val integrationName: String, private val scheduler: Scheduler) {
	fun scheduleJob(idParts: Collection<Any>, job: JobRequest)
		// create a job called "integrationName-instanceId-userId"
		= scheduler.scheduleJob("$integrationName-${idParts.joinToString("-")}", job)
}
