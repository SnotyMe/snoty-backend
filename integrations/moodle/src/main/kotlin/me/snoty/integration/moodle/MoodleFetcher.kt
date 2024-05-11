package me.snoty.integration.moodle

import kotlinx.coroutines.runBlocking
import me.snoty.backend.integration.common.*
import me.snoty.backend.integration.common.diff.EntityDiffMetrics
import me.snoty.backend.integration.common.diff.IUpdatableEntity
import me.snoty.integration.moodle.request.getCalendarUpcoming
import org.slf4j.LoggerFactory

open class MoodleFetcher(
	private val entityDiffMetrics: EntityDiffMetrics,
	private val moodleAPI: MoodleAPI = MoodleAPIImpl()
) : Fetcher<MoodleJobRequest> {
	private val logger = LoggerFactory.getLogger(javaClass)

	private fun updateStates(instanceId: InstanceId, elements: List<IUpdatableEntity<Long>>) {
		elements.forEach {
			val result = MoodleEntityStateTable.compareAndUpdateState(instanceId, it)
			entityDiffMetrics.process(result)
		}
	}

	private suspend fun fetchAssignments(moodleSettings: MoodleSettings) {
		val instanceId = moodleSettings.baseUrl.hashCode()
		val assignments = moodleAPI.getCalendarUpcoming(moodleSettings)
		updateStates(instanceId, assignments)
		logger.info("Fetched ${assignments.size} assignments for ${moodleSettings.username}")
		// TODO: send update events
	}

	class Factory(private val moodleAPI: MoodleAPI) : IntegrationFetcherFactory<MoodleJobRequest> {
		override fun create(entityDiffMetrics: EntityDiffMetrics) = MoodleFetcher(entityDiffMetrics, moodleAPI)
	}

	override fun run(jobRequest: MoodleJobRequest) {
		runBlocking {
			fetchAssignments(jobRequest.settings)
		}
	}
}
