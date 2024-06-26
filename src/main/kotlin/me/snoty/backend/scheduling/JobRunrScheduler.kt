package me.snoty.backend.scheduling

import org.jobrunr.scheduling.BackgroundJob
import org.jobrunr.scheduling.BackgroundJobRequest
import org.jobrunr.scheduling.RecurringJobBuilder.aRecurringJob
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class JobRunrScheduler : Scheduler {
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
