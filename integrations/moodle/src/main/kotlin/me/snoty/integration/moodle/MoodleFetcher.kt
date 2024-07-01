package me.snoty.integration.moodle

import kotlinx.coroutines.flow.collect
import me.snoty.backend.utils.contextual
import me.snoty.integration.common.NodeContext
import me.snoty.integration.common.fetch.AbstractIntegrationFetcher
import me.snoty.integration.common.fetch.FetchContext
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.moodle.request.getCalendarUpcoming
import org.jobrunr.jobs.context.JobContext
import org.jobrunr.jobs.context.JobDashboardLogger

open class MoodleFetcher(
	private val nodeContext: NodeContext,
	private val moodleAPI: MoodleAPI = MoodleAPIImpl()
) : AbstractIntegrationFetcher() {
	override val position = NodePosition.START

	private suspend fun FetchContext.fetchAssignments(
		node: IFlowNode,
		logger: JobDashboardLogger,
	) = nodeContext.contextual {
		val moodleSettings = node.getConfig<MoodleSettings>(codecRegistry)

		val assignments = fetchStage {
			moodleAPI.getCalendarUpcoming(moodleSettings)
		}

		updateStage {
			entityStateService.updateStates(node, assignments)
		}

		logger.info("Fetched ${assignments.size} assignments for ${moodleSettings.username}")

		flowStage {
			flowService.runFlow(node, assignments)
				// TODO: do something with result
				.collect()
		}
	}

	override suspend fun process(node: IFlowNode, input: EdgeVertex): EdgeVertex {
		val jobContext = input as JobContext
		val logger = logger(jobContext)
		val progress = progress(jobContext, 1)
		progress.fetchAssignments(node, logger)

		return EdgeVertices.START_OF_FLOW
	}
}
