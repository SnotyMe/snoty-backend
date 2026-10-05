package me.snoty.backend.scheduling.jobrunr

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.scheduling.JobTriggerResult
import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.scheduling.SnotyJob
import org.jobrunr.configuration.JobRunr
import org.jobrunr.jobs.Job
import org.jobrunr.jobs.states.StateName
import org.jobrunr.scheduling.JobBuilder
import org.jobrunr.scheduling.JobBuilder.aJob
import org.jobrunr.scheduling.JobRequestScheduler
import org.jobrunr.scheduling.RecurringJobBuilder.aRecurringJob
import org.jobrunr.server.BackgroundJobServer
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible

@Single
@Named("adapter")
class JobRunrScheduler(
	private val jobRunrConfigurer: JobRunrConfigurer,
	private val storageProvider: SnotyJobRunrStorageProvider,
) : Scheduler {
	private val logger = KotlinLogging.logger {}

	private lateinit var jobRequestScheduler: JobRequestScheduler
	private lateinit var backgroundJobServer: BackgroundJobServer
	private lateinit var build: KFunction<Job>
	private lateinit var saveJob: KFunction<*>

	@Suppress("UNCHECKED_CAST")
	override fun start() {
		jobRequestScheduler = jobRunrConfigurer.initialize().jobRequestScheduler
		backgroundJobServer = JobRunr.getBackgroundJobServer()

		build = JobBuilder::class
			.declaredMemberFunctions
			.single { it.name == "build" && it.valueParameters.isEmpty() }
			.apply {
				isAccessible = true
			} as KFunction<Job>

		saveJob = jobRequestScheduler::class
			.memberFunctions
			.single { it.name == "saveJob" }
			.apply {
				isAccessible = true
			}
	}

	override fun scheduleRecurringJob(job: SnotyJob) {
		jobRequestScheduler.createRecurrently(
			aRecurringJob()
				.withId(job.recurringJobId)
				.withName(job.name)
				.withAmountOfRetries(job.retries)
				.withJobRequest(job.request)
				.withSchedule(job.schedule)
		)
	}

	override fun triggerJob(job: SnotyJob): JobTriggerResult? {
		val recurringJobId = job.recurringJobId

		val jobBuilder = aJob()
			.withName(job.name)
			.withAmountOfRetries(job.retries)
			.withJobRequest(job.request)

		if (recurringJobId != null && pendingJobForRecurringJobExists(recurringJobId)) {
			logger.info { "Pending Job for ${job.recurringJobId} already exists, not scheduling" }
			return null
		}

		val job = build
			.call(jobBuilder)
			.apply {
				setRecurringJobId(recurringJobId)
			}

		backgroundJobServer.processJob(job)

		return JobTriggerResult(
			jobId = job.id.toString(),
		)
	}

	override fun deleteRecurringJob(id: String) {
		jobRequestScheduler.deleteRecurringJob(id)
	}

	override fun pendingJobForRecurringJobExists(id: String) =
		storageProvider.recurringJobExists(id, StateName.ENQUEUED, StateName.PROCESSING)
}
