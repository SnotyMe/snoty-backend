package me.snoty.backend.scheduling

import org.slf4j.event.Level
import org.jobrunr.jobs.lambdas.JobRequest as JobRunrRequest
import org.jobrunr.jobs.lambdas.JobRequestHandler as JobRunrRequestHandler

typealias JobRequest = JobRunrRequest
typealias JobRequestHandler<R> = JobRunrRequestHandler<R>

data class SnotyJob(
	val name: String,
	val request: JobRequest
)

data class FlowJobRequest(
	val logLevel: Level = Level.INFO,
)
