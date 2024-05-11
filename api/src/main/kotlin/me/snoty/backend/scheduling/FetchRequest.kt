package me.snoty.backend.scheduling

import org.jobrunr.jobs.lambdas.JobRequest as JobRunrRequest
import org.jobrunr.jobs.lambdas.JobRequestHandler as JobRunrRequestHandler

typealias JobRequest = JobRunrRequest
typealias JobRequestHandler<R> = JobRunrRequestHandler<R>
