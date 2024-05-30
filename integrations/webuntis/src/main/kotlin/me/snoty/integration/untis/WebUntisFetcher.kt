package me.snoty.integration.untis

import kotlinx.coroutines.runBlocking
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.fetch.AbstractIntegrationFetcher
import me.snoty.integration.common.fetch.FetchProgress
import me.snoty.integration.common.fetch.IntegrationFetcherFactory
import me.snoty.integration.common.fetch.IntegrationProgressState
import me.snoty.integration.untis.request.getExams
import org.jobrunr.jobs.context.JobDashboardLogger
import java.util.*

class WebUntisFetcher(
	private val entityStateService: EntityStateService,
	private val untis: WebUntisAPI = WebUntisAPIImpl()
) : AbstractIntegrationFetcher<WebUntisJobRequest>() {
	private suspend fun fetchExams(
		logger: JobDashboardLogger,
		progress: FetchProgress,
		untisSettings: WebUntisSettings,
		userId: UUID
	) {
		val instanceId = untisSettings.instanceId

		progress.advance(IntegrationProgressState.FETCHING)
		val exams = untis.getExams(untisSettings)

		progress.advance(IntegrationProgressState.UPDATING_IN_DB)
		entityStateService.updateStates(userId, instanceId, exams)

		progress.advance(IntegrationProgressState.STAGE_DONE)
		logger.info("Fetched ${exams.size} exams for ${untisSettings.username}")
	}

	class Factory(private val untis: WebUntisAPI) : IntegrationFetcherFactory<WebUntisJobRequest, Int> {
		override fun create(entityStateService: EntityStateService)
			= WebUntisFetcher(entityStateService, untis)
	}

	override fun run(jobRequest: WebUntisJobRequest) = runBlocking {
		val context = jobContext()
		val logger = logger(context)
		val progress = progress(context, 1)
		fetchExams(logger, progress, jobRequest.settings, jobRequest.userId)
	}
}
