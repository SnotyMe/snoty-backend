package me.snoty.backend.scheduling.jobrunr

import me.snoty.backend.scheduling.JobSchedule
import org.jobrunr.scheduling.RecurringJobBuilder
import kotlin.time.toJavaDuration

fun RecurringJobBuilder.withSchedule(schedule: JobSchedule) = when (schedule) {
	is JobSchedule.Recurring -> withInterval(schedule.interval.toJavaDuration())
	is JobSchedule.Cron -> withCron(schedule.expression)
	else -> error("Unsupported schedule type: $schedule")
}
