package me.snoty.integration.moodle

import kotlinx.coroutines.runBlocking
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.fetch.*
import me.snoty.integration.moodle.request.getCalendarUpcoming
import org.jobrunr.jobs.context.JobDashboardLogger
import java.util.*

open class MoodleFetcher(
	private val entityStateService: EntityStateService,
	private val moodleAPI: MoodleAPI = MoodleAPIImpl()
) : AbstractIntegrationFetcher<MoodleJobRequest>() {
	private suspend fun FetchContext.fetchAssignments(logger: JobDashboardLogger, progress: FetchProgress, moodleSettings: MoodleSettings, userId: UUID) {
		val instanceId = moodleSettings.instanceId

		val assignments = fetch {
			moodleAPI.getCalendarUpcoming(moodleSettings)
		}

		updateStates {
			entityStateService.updateStates(userId, instanceId, assignments)
		}

		progress.advance(IntegrationProgressState.STAGE_DONE)
		logger.info("Fetched ${assignments.size} assignments for ${moodleSettings.username}")
		// TODO: send update events
	}

	class Factory(private val moodleAPI: MoodleAPI) : IntegrationFetcherFactory<MoodleJobRequest, Long> {
		override fun create(entityStateService: EntityStateService)
			= MoodleFetcher(entityStateService, moodleAPI)
	}

	override fun run(jobRequest: MoodleJobRequest) = runBlocking {
		val context = jobContext()
		val logger = logger(context)
		val progress = progress(context, 1)
		progress.fetchAssignments(logger, progress, jobRequest.settings, jobRequest.userId)
	}
}
