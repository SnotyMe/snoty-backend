package me.snoty.backend.scheduling

import org.jobrunr.scheduling.BackgroundJob
import org.jobrunr.scheduling.RecurringJobBuilder
import org.jobrunr.scheduling.RecurringJobBuilder.aRecurringJob
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class JobRunrUtils(private val integrationName: String) {
	fun scheduleJob(id: Collection<Any>, customizer: RecurringJobBuilder.() -> Unit) {
		BackgroundJob.createRecurrently(
			aRecurringJob()
				.withId("$integrationName-${id.joinToString("-")}")
				.withDuration(15.minutes.toJavaDuration())
				.apply { customizer() }
		)
	}
}
