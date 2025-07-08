package me.snoty.backend.scheduling.jobrunr

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import me.snoty.backend.utils.randomV7
import org.jobrunr.jobs.Job
import org.jobrunr.jobs.JobDetails
import org.jobrunr.jobs.mappers.JobMapper
import org.jobrunr.jobs.states.StateName
import org.jobrunr.kotlin.utils.mapper.KotlinxSerializationJsonMapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

abstract class JobRunrStorageProviderSpec {
	abstract val storageProvider: SnotyJobRunrStorageProvider

	@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
	@BeforeAll
	fun setup() {
		storageProvider.setJobMapper(JobMapper(KotlinxSerializationJsonMapper()))
	}

	fun aJob() = Job(Uuid.randomV7().toJavaUuid(), JobDetails("", "", "", emptyList())).apply {
		jobName = "test-job"
	}

	@Test
	fun `recurring job doesn't exist with no states`() {
		val recurringJobId = "test-recurring-job"
		assertFalse(storageProvider.recurringJobExists(recurringJobId))
	}

	@Test
	fun `recurring job exists with specific states`() {
		val id = "exists-with-specific-states"
		val job = aJob()
		job.setRecurringJobId(id)
		storageProvider.save(job)
		assertTrue(storageProvider.recurringJobExists(id))
	}

	@Test
	fun `recurring job exists with its state`() {
		val id = "exists-with-its-state"
		val job = aJob()
		job.setRecurringJobId(id)
		storageProvider.save(job)
		assertTrue(storageProvider.recurringJobExists(id, job.state))
	}

	@Test
	fun `recurring job doesn't exist with different state`() {
		val id = "does-not-exist-with-different-state"
		val job = aJob()
		job.setRecurringJobId(id)
		storageProvider.save(job)
		assertFalse(storageProvider.recurringJobExists(id, StateName.DELETED))
	}

	@Test
	fun `recurring job exists enqueued`() {
		val id = "exists-enqueued"
		val job = aJob()
		job.setRecurringJobId(id)
		storageProvider.save(job)
		assertTrue(storageProvider.recurringJobExists(id, StateName.ENQUEUED))
	}
}
