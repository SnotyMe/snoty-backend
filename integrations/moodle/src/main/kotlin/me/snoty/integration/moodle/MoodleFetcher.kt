package me.snoty.integration.moodle

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import me.snoty.integration.common.IntegrationFetcher
import me.snoty.integration.common.IntegrationFetcherFactory
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.moodle.request.getCalendarUpcoming
import java.util.*

open class MoodleFetcher(
	private val entityStateService: EntityStateService,
	private val moodleAPI: MoodleAPI = MoodleAPIImpl()
) : IntegrationFetcher<MoodleJobRequest> {
	private val logger = KotlinLogging.logger {}

	private suspend fun fetchAssignments(moodleSettings: MoodleSettings, userId: UUID) {
		val instanceId = moodleSettings.instanceId
		val assignments = moodleAPI.getCalendarUpcoming(moodleSettings)
		entityStateService.updateStates(userId, instanceId, assignments)
		logger.info { "Fetched ${assignments.size} assignments for ${moodleSettings.username}" }
		// TODO: send update events
	}

	class Factory(private val moodleAPI: MoodleAPI) : IntegrationFetcherFactory<MoodleJobRequest, Long> {
		override fun create(entityStateService: EntityStateService)
			= MoodleFetcher(entityStateService, moodleAPI)
	}

	override fun run(jobRequest: MoodleJobRequest) = runBlocking {
		fetchAssignments(jobRequest.settings, jobRequest.userId)
	}
}
