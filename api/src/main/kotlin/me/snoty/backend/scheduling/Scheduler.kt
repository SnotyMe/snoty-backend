package me.snoty.backend.scheduling

import org.jobrunr.jobs.lambdas.JobRequest

interface Scheduler {
	fun scheduleJob(id: String, job: () -> Unit)

	fun scheduleJob(id: String, job: JobRequest)
}
