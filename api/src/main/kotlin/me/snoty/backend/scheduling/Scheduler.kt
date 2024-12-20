package me.snoty.backend.scheduling

interface Scheduler {
	fun start()

	fun scheduleJob(job: SnotyJob)
	fun scheduleRecurringJob(id: String, job: SnotyJob)
	fun deleteRecurringJob(id: String)
}
