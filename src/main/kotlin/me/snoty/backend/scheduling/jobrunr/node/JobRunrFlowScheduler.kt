package me.snoty.backend.scheduling.jobrunr.node

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNot
import me.snoty.backend.scheduling.FlowJobRequest
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.scheduling.SnotyJob
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
			request = jobRunrRequest,
		)
		return job
	}

	override fun schedule(workflow: Workflow, jobRequest: FlowJobRequest) {
		jobRunrScheduler.scheduleRecurringJob(jobId(workflow), createJob(workflow, jobRequest))
	}

	override fun trigger(workflow: Workflow, jobRequest: FlowJobRequest) {
		jobRunrScheduler.scheduleJob(createJob(workflow, jobRequest))
	}

	override suspend fun scheduleMissing(flowService: FlowService) {
		logger.debug { "Starting flow scheduler" }
		val existingJobs = storageProvider.recurringJobs

		flowService.getAll()
			.catch { e -> logger.error(e) { "Failed to get flows to schedule" } }
			.filterNot { workflow ->
				existingJobs.any { job ->
					job.id == jobId(workflow)
				}
			}
			.collect { schedule(it) }
	}

	override fun deleteAll(workflow: Workflow) {
		jobRunrScheduler.deleteRecurringJob(jobId(workflow))
	}
}
