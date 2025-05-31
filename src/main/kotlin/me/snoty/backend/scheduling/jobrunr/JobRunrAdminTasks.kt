package me.snoty.backend.scheduling.jobrunr

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import me.snoty.backend.scheduling.AdminTasks
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.scheduling.Task
import me.snoty.integration.common.wiring.flow.FlowService
import org.jobrunr.jobs.states.StateName
import org.jobrunr.storage.StorageProvider
import org.koin.core.annotation.Single
import java.time.Instant as JavaInstant

@Single
class JobRunrAdminTasks(private val flowScheduler: FlowScheduler, private val flowService: FlowService, private val storageProvider: StorageProvider) : AdminTasks {
	private val logger = KotlinLogging.logger {}

	private suspend fun scheduleMissingJobs() {
		flowScheduler.scheduleMissing(flowService)
	}

	private suspend fun renameExistingJobs() {
		val existingJobs = storageProvider.recurringJobs
		flowService.getAll()
			.catch { e -> logger.error(e) { "Failed to schedule flows" } }
			.filter { workflow ->
				val existingJob = existingJobs.firstOrNull { job ->
					job.id == flowScheduler.jobId(workflow)
				} ?: return@filter false
				existingJob.jobName != flowScheduler.jobName(workflow)
			}
			.collect {
				logger.trace { "Renaming job for $it" }
				flowScheduler.schedule(it)
			}
	}

	private fun deleteFailedJobs() {
		storageProvider.deleteJobsPermanently(StateName.FAILED, JavaInstant.now())
	}

	override fun getTasks() = listOf(
		Task("Schedule missing jobs", ::scheduleMissingJobs),
		Task("Rename existing jobs", ::renameExistingJobs),
		Task("Delete failed jobs", ::deleteFailedJobs)
	)
}
