package me.snoty.backend.scheduling

import org.jobrunr.scheduling.BackgroundJob
import org.jobrunr.scheduling.RecurringJobBuilder.aRecurringJob
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class JobRunrScheduler : Scheduler {
	override fun scheduleJob(id: String, job: () -> Unit) {
		BackgroundJob.createRecurrently(
			aRecurringJob()
				.withId(id)
				.withDuration(15.minutes.toJavaDuration())
				.apply {
					this.withDetails {
						job()
					}
				}
		)
	}
}