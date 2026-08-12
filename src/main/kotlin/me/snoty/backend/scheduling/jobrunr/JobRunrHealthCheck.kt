package me.snoty.backend.scheduling.jobrunr

import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckResult
import com.sksamuel.cohort.HealthStatus
import org.jobrunr.configuration.JobRunr
import org.koin.core.annotation.Single

@Single
class JobRunrHealthCheck : HealthCheck {
	private val backgroundJobServer = JobRunr.getBackgroundJobServer()

	override val name = "jobrunr_running"

	override suspend fun check(): HealthCheckResult = when {
		backgroundJobServer.isRunning() -> HealthCheckResult(
			status = HealthStatus.Healthy,
			message = "JobRunr Background Job Server is running",
			cause = null,
		)

		else -> HealthCheckResult(
			status = HealthStatus.Unhealthy,
			message = "JobRunr Background Job Server is not running",
			cause = null,
		)
	}
}
