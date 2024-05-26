package me.snoty.integration.untis

import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import me.snoty.integration.common.InstanceId
import me.snoty.integration.common.IntegrationFetcher
import me.snoty.integration.common.IntegrationFetcherFactory
import me.snoty.integration.common.diff.*
import me.snoty.integration.common.diff.state.UserEntityStates
import me.snoty.integration.common.diff.state.updateEntitiesInDB
import me.snoty.integration.untis.request.getExams
import java.util.*

class WebUntisFetcher(
	private val entityDiffMetrics: EntityDiffMetrics,
	private val stateCollection: WebUntisStateCollection,
	private val changesCollection: WebUntisChangesCollection,
	private val untis: WebUntisAPI = WebUntisAPIImpl()
) : IntegrationFetcher<WebUntisJobRequest> {
	private val logger = KotlinLogging.logger {}

	private suspend fun updateStates(instanceId: InstanceId, elements: List<IUpdatableEntity<Int>>, userId: UUID) {
		updateEntitiesInDB(entityDiffMetrics, stateCollection, changesCollection, userId, instanceId, elements)
	}

	private suspend fun fetchExams(untisSettings: WebUntisSettings, userId: UUID) {
		val instanceId = untisSettings.instanceId
		val exams = untis.getExams(untisSettings)
		updateStates(instanceId, exams, userId)
		logger.info { "Fetched ${exams.size} exams for ${untisSettings.username}" }
	}

	class Factory(private val untis: WebUntisAPI) : IntegrationFetcherFactory<WebUntisJobRequest, Int> {
		override fun create(
			entityDiffMetrics: EntityDiffMetrics,
			stateCollection: MongoCollection<UserEntityStates>,
			changesCollection: MongoCollection<UserEntityChanges>
		)
			= WebUntisFetcher(entityDiffMetrics, stateCollection, changesCollection, untis)
	}

	override fun run(jobRequest: WebUntisJobRequest) = runBlocking {
		fetchExams(jobRequest.settings, jobRequest.userId)
	}
}
