package me.snoty.integration.moodle

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import me.snoty.integration.common.IntegrationFetcher
import me.snoty.integration.common.InstanceId
import me.snoty.integration.common.IntegrationFetcherFactory
import me.snoty.integration.common.diff.EntityDiffMetrics
import me.snoty.integration.common.diff.IUpdatableEntity
import me.snoty.integration.moodle.request.getCalendarUpcoming
import java.util.UUID

open class MoodleFetcher(
	private val entityDiffMetrics: EntityDiffMetrics,
	private val moodleAPI: MoodleAPI = MoodleAPIImpl()
) : IntegrationFetcher<MoodleJobRequest> {
	private val logger = KotlinLogging.logger {}

	private fun updateStates(elements: List<IUpdatableEntity<Long>>, instanceId: InstanceId, userId: UUID) {
		elements.forEach {
			val result = MoodleEntityStateTable.compareAndUpdateState(it, instanceId, userId)
			entityDiffMetrics.process(result)
		}
	}

	private suspend fun fetchAssignments(moodleSettings: MoodleSettings, userId: UUID) {
		val instanceId = moodleSettings.instanceId
		val assignments = moodleAPI.getCalendarUpcoming(moodleSettings)
		updateStates(assignments, instanceId, userId)
		logger.info { "Fetched ${assignments.size} assignments for ${moodleSettings.username}" }
		// TODO: send update events
	}

	class Factory(private val moodleAPI: MoodleAPI) : IntegrationFetcherFactory<MoodleJobRequest> {
		override fun create(entityDiffMetrics: EntityDiffMetrics) = MoodleFetcher(entityDiffMetrics, moodleAPI)
	}

	override fun run(jobRequest: MoodleJobRequest) = runBlocking {
		fetchAssignments(jobRequest.settings, jobRequest.userId)
	}
}
