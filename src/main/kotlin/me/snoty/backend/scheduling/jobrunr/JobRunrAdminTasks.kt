package me.snoty.backend.scheduling.jobrunr

import me.snoty.backend.scheduling.AdminTasks
import me.snoty.backend.scheduling.Task
import org.jobrunr.jobs.states.StateName
import org.jobrunr.storage.StorageProvider
import org.koin.core.annotation.Single
import java.time.Instant as JavaInstant

@Single
class JobRunrAdminTasks(private val storageProvider: StorageProvider) : AdminTasks {
	private fun deleteFailedJobs() {
		storageProvider.deleteJobsPermanently(StateName.FAILED, JavaInstant.now())
	}

	override fun getTasks() = listOf(
		Task("Delete failed jobs", ::deleteFailedJobs)
	)
}
