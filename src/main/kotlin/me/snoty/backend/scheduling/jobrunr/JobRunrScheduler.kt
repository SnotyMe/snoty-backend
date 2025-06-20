package me.snoty.backend.scheduling.jobrunr

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.scheduling.JobSchedule
import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.scheduling.SnotyJob
import org.jobrunr.jobs.Job
import org.jobrunr.jobs.states.StateName
import org.jobrunr.scheduling.JobBuilder
import org.jobrunr.scheduling.JobBuilder.aJob
import org.jobrunr.scheduling.JobRequestScheduler
import org.jobrunr.scheduling.RecurringJobBuilder.aRecurringJob
import org.koin.core.annotation.Single
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.isAccessible
import kotlin.time.toJavaDuration

@Single
class JobRunrScheduler(private val jobRunrConfigurer: JobRunrConfigurer, private val storageProvider: SnotyJobrunrStorageProvider) : Scheduler {
	private val logger = KotlinLogging.logger {}

	private lateinit var jobRequestScheduler: JobRequestScheduler
	private lateinit var build: KFunction<Job>
	private lateinit var saveJob: KFunction<*>

	@Suppress("UNCHECKED_CAST")
	override fun start() {
		jobRequestScheduler = jobRunrConfigurer.initialize().jobRequestScheduler

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

	override fun scheduleRecurringJob(id: String, job: SnotyJob) {
		jobRequestScheduler.createRecurrently(
			aRecurringJob()
				.withId(id)
				.withName(job.name)
				.withAmountOfRetries(job.retries)
				.withJobRequest(job.request)
				.apply {
					when (val schedule = job.schedule) {
						is JobSchedule.Recurring -> withInterval(schedule.interval.toJavaDuration())
						is JobSchedule.Cron -> withCron(schedule.expression)
						else -> error("Unsupported schedule type: $schedule")
					}
				}
		)
	}

	@Suppress("ERROR_SUPPRESSION")
	override fun triggerRecurringJobOrSchedule(job: SnotyJob) {
		val recurringJobId = job.recurringJobId

		val jobBuilder = aJob()
			.withName(job.name)
			.withAmountOfRetries(job.retries)
			.withJobRequest(job.request)

		if (recurringJobId == null) {
			jobRequestScheduler.create(jobBuilder)
			return
		}

		if (recurringJobExists(recurringJobId)) {
			logger.info { "Job ${job.recurringJobId} already exists, not scheduling" }
			return
		}

		val job = build
			.call(jobBuilder)
			.apply {
				setRecurringJobId(recurringJobId)
			}

		saveJob.call(jobRequestScheduler, job)
	}

	override fun deleteRecurringJob(id: String) {
		jobRequestScheduler.deleteRecurringJob(id)
	}

	override fun recurringJobExists(id: String) =
		storageProvider.recurringJobExists(id, StateName.ENQUEUED, StateName.PROCESSING)
}
