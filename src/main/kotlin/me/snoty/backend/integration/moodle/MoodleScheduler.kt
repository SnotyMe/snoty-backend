package me.snoty.backend.integration.moodle

import kotlinx.coroutines.runBlocking
import me.snoty.backend.integration.common.InstanceId
import me.snoty.backend.integration.common.IntegrationConfig
import me.snoty.backend.integration.common.IntegrationScheduler
import me.snoty.backend.integration.common.IntegrationSchedulerFactory
import me.snoty.backend.integration.common.diff.EntityDiffMetrics
import me.snoty.backend.integration.common.diff.IUpdatableEntity
import me.snoty.backend.integration.moodle.request.getCalendarUpcoming
import me.snoty.backend.scheduling.JobRunrUtils
import org.slf4j.LoggerFactory

open class MoodleScheduler(private val entityDiffMetrics: EntityDiffMetrics, private val moodleAPI: MoodleAPI = MoodleAPIImpl()) : IntegrationScheduler<MoodleSettings> {
	private val jobRunrUtils = JobRunrUtils("moodle")
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

	override fun schedule(config: IntegrationConfig<MoodleSettings>) {
		val instanceId = config.settings.baseUrl.hashCode()
		jobRunrUtils.scheduleJob(listOf(instanceId, config.user), customizer = {
			// TODO: inline this, unfortunately, jobrunr is doing some weird stuff with the JobActivator
			this.withDetails {
				fetchAll(config)
			}
		})
	}

	open fun fetchAll(config: IntegrationConfig<MoodleSettings>) {
		runBlocking {
			fetchAssignments(config.settings)
		}
	}

	class Factory(private val moodleAPI: MoodleAPI) : IntegrationSchedulerFactory<MoodleSettings> {
		override fun create(entityDiffMetrics: EntityDiffMetrics): IntegrationScheduler<MoodleSettings> {
			return MoodleScheduler(entityDiffMetrics, moodleAPI)
		}
	}
}
