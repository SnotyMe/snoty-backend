package me.snoty.backend.scheduling

interface Scheduler {
	fun scheduleJob(job: SnotyJob)
	fun scheduleRecurringJob(id: String, job: SnotyJob)
}
