package me.snoty.backend.scheduling.jobrunr

import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.scheduling.SnotyJob
import org.jobrunr.scheduling.BackgroundJob
import org.jobrunr.scheduling.BackgroundJobRequest
import org.jobrunr.scheduling.RecurringJobBuilder.aRecurringJob
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

@Single
class JobRunrScheduler(jobRunrConfigurer: JobRunrConfigurer) : Scheduler {
	init {
		jobRunrConfigurer.configure()
	}

	override fun scheduleJob(id: String, job: () -> Unit) {
		BackgroundJob.createRecurrently(
			aRecurringJob()
				.withId(id)
				.withDuration(15.minutes.toJavaDuration())
				.withAmountOfRetries(5)
				.apply {
					this.withDetails {
						job()
					}
				}
		)
	}

	override fun scheduleJob(id: String, job: SnotyJob) {
		BackgroundJobRequest.createRecurrently(
			aRecurringJob()
				.withId(id)
				.withName(job.name)
				.withDuration(15.minutes.toJavaDuration())
				.withAmountOfRetries(5)
				.withJobRequest(job.request)
		)
	}
}
