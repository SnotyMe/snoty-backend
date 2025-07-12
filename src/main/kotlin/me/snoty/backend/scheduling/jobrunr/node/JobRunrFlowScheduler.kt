package me.snoty.backend.scheduling.jobrunr.node

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import me.snoty.backend.scheduling.*
import me.snoty.backend.scheduling.jobrunr.JobRunrScheduler
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.flow.Workflow
import org.jobrunr.storage.StorageProvider
import org.koin.core.annotation.Single

@Single
class JobRunrFlowScheduler(
	private val jobRunrScheduler: JobRunrScheduler,
	private val storageProvider: StorageProvider,
) : FlowScheduler {
	private val logger = KotlinLogging.logger {}

	private fun createJob(workflow: Workflow, jobRequest: FlowJobRequest): SnotyJob {
		val jobRunrRequest = jobRequest.run {
			JobRunrFlowJobRequest(
				flowId = workflow._id,
				triggeredBy = triggeredBy,
				logLevel = logLevel,
			)
		}
		val job = SnotyJob(
			name = jobName(workflow),
			retries = jobRequest.retries,
			schedule = workflow.settings.schedule,
			recurringJobId = jobId(workflow),
			request = jobRunrRequest,
		)
		return job
	}

	override fun schedule(workflow: Workflow, jobRequest: FlowJobRequest) {
		if (workflow.settings.schedule is JobSchedule.Never) {
			logger.debug { "Not scheduling job for workflow ${workflow._id} as it is set to never run" }
			return
		}
		jobRunrScheduler.scheduleRecurringJob(jobId(workflow), createJob(workflow, jobRequest))
	}

	override fun trigger(workflow: Workflow, jobRequest: FlowJobRequest) {
		jobRunrScheduler.triggerRecurringJobOrSchedule(createJob(workflow, jobRequest))
	}

	override suspend fun scheduleMissing(flowService: FlowService) {
		logger.debug { "Starting flow scheduler" }
		val existingJobs = storageProvider.recurringJobs

		flowService.getAll()
			.catch { e -> logger.error(e) { "Failed to get flows to schedule" } }
			.filter { workflow ->
				val jobId = jobId(workflow)
				val job = existingJobs.firstOrNull { it.id == jobId } ?: return@filter true

				val gottenExpression = workflow.settings.schedule.toJobRunrScheduleExpression()
				val expressionsMatch = job.scheduleExpression != gottenExpression

				if (expressionsMatch) {
					logger.trace { "Rescheduling job $jobId as schedule changed from ${job.scheduleExpression} to $gottenExpression" }
				}

				expressionsMatch
			}
			.filterNot { workflow ->
				workflow.settings.schedule is JobSchedule.Never
			}
			.collect { schedule(it) }
	}

	fun JobSchedule.toJobRunrScheduleExpression() = when (this) {
		is JobSchedule.Cron -> this.expression
		is JobSchedule.Recurring -> this.interval.toIsoString()
		JobSchedule.Never -> null
	}

	override suspend fun reschedule(workflow: Workflow) = when (workflow.settings.schedule) {
		JobSchedule.Never -> jobRunrScheduler.deleteRecurringJob(jobId(workflow))
		else -> jobRunrScheduler.scheduleRecurringJob(jobId(workflow), createJob(workflow, DEFAULT_FLOW_JOB_REQUEST))
	}

	override fun deleteAll(workflow: Workflow) {
		jobRunrScheduler.deleteRecurringJob(jobId(workflow))
	}
}
