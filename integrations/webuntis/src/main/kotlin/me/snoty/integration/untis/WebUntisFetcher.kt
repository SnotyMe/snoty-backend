package me.snoty.integration.untis

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import me.snoty.integration.common.InstanceId
import me.snoty.integration.common.IntegrationFetcher
import me.snoty.integration.common.IntegrationFetcherFactory
import me.snoty.integration.common.diff.EntityDiffMetrics
import me.snoty.integration.common.diff.IUpdatableEntity
import me.snoty.integration.untis.request.getExams

class WebUntisFetcher(
	private val entityDiffMetrics: EntityDiffMetrics,
	private val untis: WebUntisAPI = WebUntisAPIImpl()
) : IntegrationFetcher<WebUntisJobRequest> {
	private val logger = KotlinLogging.logger {}

	private fun updateStates(instanceId: InstanceId, elements: List<IUpdatableEntity<Int>>) {
		elements.forEach {
			val result = WebUntisEntityStateTable.compareAndUpdateState(instanceId, it)
			entityDiffMetrics.process(result)
		}
	}

	private suspend fun fetchExams(untisSettings: WebUntisSettings) {
		val instanceId = untisSettings.baseUrl.hashCode()
		val exams = untis.getExams(untisSettings)
		updateStates(instanceId, exams)
		logger.info { "Fetched ${exams.size} exams for ${untisSettings.username}" }
	}

	class Factory(private val untis: WebUntisAPI) : IntegrationFetcherFactory<WebUntisJobRequest> {
		override fun create(entityDiffMetrics: EntityDiffMetrics) = WebUntisFetcher(entityDiffMetrics, untis)
	}

	override fun run(jobRequest: WebUntisJobRequest) = runBlocking {
		fetchExams(jobRequest.settings)
	}
}
