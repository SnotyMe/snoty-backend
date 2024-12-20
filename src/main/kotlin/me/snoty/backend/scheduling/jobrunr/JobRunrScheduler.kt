package me.snoty.backend.scheduling.jobrunr

import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.scheduling.SnotyJob
import org.jobrunr.scheduling.JobBuilder.aJob
import org.jobrunr.scheduling.JobRequestScheduler
import org.jobrunr.scheduling.RecurringJobBuilder.aRecurringJob
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Single
class JobRunrScheduler(private val jobRunrConfigurer: JobRunrConfigurer) : Scheduler {
	private lateinit var jobRequestScheduler: JobRequestScheduler

	override fun start() {
		jobRequestScheduler = jobRunrConfigurer.initialize().jobRequestScheduler
	}

	override fun scheduleRecurringJob(id: String, job: SnotyJob) {
		jobRequestScheduler.createRecurrently(
			aRecurringJob()
				.withId(id)
				.withName(job.name)
				.withDuration(15.minutes.toJavaDuration())
				.withAmountOfRetries(job.retries)
				.withJobRequest(job.request)
		)
	}

	override fun scheduleJob(job: SnotyJob) {
		jobRequestScheduler.create(
			aJob()
				.withName(job.name)
				.withAmountOfRetries(job.retries)
				.withJobRequest(job.request)
		)
	}

	override fun deleteRecurringJob(id: String) {
		jobRequestScheduler.deleteRecurringJob(id)
	}
}
