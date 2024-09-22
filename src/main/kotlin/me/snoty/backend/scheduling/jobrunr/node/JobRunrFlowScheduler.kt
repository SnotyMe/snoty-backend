package me.snoty.backend.scheduling.impl.jobrunr.node

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNot
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.scheduling.impl.jobrunr.JobRunrScheduler
import me.snoty.integration.common.utils.createFlowJob
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

	override fun schedule(workflow: Workflow) {
		val jobRequest = JobRunrFlowJobRequest(workflow._id)
		val job = createFlowJob(workflow, jobRequest)
		jobRunrScheduler.scheduleJob(jobId(workflow), job)
	}

	override suspend fun scheduleMissing(flowService: FlowService) {
		logger.debug { "Starting flow scheduler" }
		val existingJobs = storageProvider.recurringJobs

		flowService.getAll()
			.catch { e -> logger.error(e) { "Failed to schedule flows" } }
			.filterNot { workflow ->
				existingJobs.any { job ->
					job.id == jobId(workflow)
				}
			}
			.collect { schedule(it) }
	}
}
