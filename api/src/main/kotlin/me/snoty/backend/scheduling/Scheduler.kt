package me.snoty.backend.scheduling

interface Scheduler {
	fun start()

	fun triggerJob(job: SnotyJob): JobTriggerResult?
	fun scheduleRecurringJob(job: SnotyJob)
	fun deleteRecurringJob(id: String)

	fun pendingJobForRecurringJobExists(id: String): Boolean
}
