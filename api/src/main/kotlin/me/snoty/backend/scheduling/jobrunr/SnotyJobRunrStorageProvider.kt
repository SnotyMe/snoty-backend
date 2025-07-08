package me.snoty.backend.scheduling.jobrunr

import org.jobrunr.jobs.states.StateName
import org.jobrunr.storage.StorageProvider

interface SnotyJobRunrStorageProvider : StorageProvider {
	fun recurringJobExists(recurringJobId: String, vararg states: StateName): Boolean
}
