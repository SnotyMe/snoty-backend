package me.snoty.backend.scheduling

interface Scheduler {
	fun scheduleJob(id: String, job: () -> Unit)

	fun scheduleJob(id: String, job: JobRequest)
}
