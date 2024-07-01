package me.snoty.integration.moodle

import me.snoty.integration.common.NodeContext
import me.snoty.integration.common.fetch.AbstractIntegrationFetcher
import me.snoty.integration.common.fetch.FetchContext
import me.snoty.integration.common.wiring.EdgeVertex
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.moodle.request.getCalendarUpcoming
import org.jobrunr.jobs.context.JobContext
import kotlin.reflect.KClass

open class MoodleFetcher(
	private val nodeContext: NodeContext,
	private val moodleAPI: MoodleAPI = MoodleAPIImpl()
) : AbstractIntegrationFetcher() {
	override val position = NodePosition.START
	override val settingsClass: KClass<out NodeSettings> = MoodleSettings::class

	private suspend fun FetchContext.fetchAssignments(
		node: IFlowNode,
	) = nodeContext.run {
		val moodleSettings = node.getConfig<MoodleSettings>(codecRegistry)

		val assignments = fetchStage {
			moodleAPI.getCalendarUpcoming(moodleSettings)
		}

		updateStage {
			entityStateService.updateStates(node, assignments)
		}

		logger.info("Fetched ${assignments.size} assignments for ${moodleSettings.username}")

		return@run assignments
	}

	override suspend fun process(node: IFlowNode, input: EdgeVertex): EdgeVertex {
		val jobContext = input as JobContext
		val progress = progress(jobContext, 1)

		return progress.fetchAssignments(node)
	}
}
