package me.snoty.backend.scheduling

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.event.Level
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.jobrunr.jobs.lambdas.JobRequest as JobRunrRequest
import org.jobrunr.jobs.lambdas.JobRequestHandler as JobRunrRequestHandler

typealias JobRequest = JobRunrRequest
typealias JobRequestHandler<R> = JobRunrRequestHandler<R>

val DEFAULT_SCHEDULE = JobSchedule.Recurring(15.minutes)

data class SnotyJob(
	val name: String,
	val retries: Int,
	val schedule: JobSchedule = DEFAULT_SCHEDULE,
	val recurringJobId: String? = null,
	val request: JobRequest,
)

@Serializable
sealed interface JobSchedule {
	@Serializable
	@SerialName("recurring")
	data class Recurring(val interval: Duration) : JobSchedule
	@Serializable
	@SerialName("cron")
	data class Cron(val expression: String) : JobSchedule
	@Serializable
	@SerialName("never")
	object Never : JobSchedule
}

data class FlowJobRequest(
	val retries: Int = 5,
	val logLevel: Level = Level.INFO,
	val triggeredBy: FlowTriggerReason,
)
