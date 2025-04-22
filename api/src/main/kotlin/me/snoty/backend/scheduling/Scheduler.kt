package me.snoty.backend.scheduling

interface Scheduler {
	fun start()

	fun triggerRecurringJobOrSchedule(job: SnotyJob)
	fun scheduleRecurringJob(id: String, job: SnotyJob)
	fun deleteRecurringJob(id: String)

	/**
	 * @return true if a Job for this RecurringJob exists
	 */
	fun recurringJobExists(id: String): Boolean
}
