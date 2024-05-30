package me.snoty.integration.untis

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import me.snoty.integration.common.IntegrationFetcher
import me.snoty.integration.common.IntegrationFetcherFactory
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.untis.request.getExams
import java.util.*

class WebUntisFetcher(
	private val entityStateService: EntityStateService,
	private val untis: WebUntisAPI = WebUntisAPIImpl()
) : IntegrationFetcher<WebUntisJobRequest> {
	private val logger = KotlinLogging.logger {}

	private suspend fun fetchExams(untisSettings: WebUntisSettings, userId: UUID) {
		val instanceId = untisSettings.instanceId
		val exams = untis.getExams(untisSettings)
		entityStateService.updateStates(userId, instanceId, exams)
		logger.info { "Fetched ${exams.size} exams for ${untisSettings.username}" }
	}

	class Factory(private val untis: WebUntisAPI) : IntegrationFetcherFactory<WebUntisJobRequest, Int> {
		override fun create(entityStateService: EntityStateService)
			= WebUntisFetcher(entityStateService, untis)
	}

	override fun run(jobRequest: WebUntisJobRequest) = runBlocking {
		fetchExams(jobRequest.settings, jobRequest.userId)
	}
}
