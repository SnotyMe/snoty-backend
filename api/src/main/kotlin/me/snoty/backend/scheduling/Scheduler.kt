package me.snoty.backend.scheduling

interface Scheduler {
	fun start()

	fun triggerRecurringJobOrSchedule(job: SnotyJob)
	fun scheduleRecurringJob(job: SnotyJob)
	fun deleteRecurringJob(id: String)

	/**
	 * @return true if a Job for this RecurringJob exists
	 */
	fun pendingJobForRecurringJobExists(id: String): Boolean
}
