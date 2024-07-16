package me.snoty.integration.moodle

import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.fetch.AbstractIntegrationFetcher
import me.snoty.integration.common.fetch.FetchContext
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.data.EmitNodeOutputContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.moodle.model.MoodleAssignment
import me.snoty.integration.moodle.request.getCalendarUpcoming
import org.jobrunr.jobs.context.JobContext
import org.slf4j.Logger
import kotlin.reflect.KClass

@RegisterNode(
	displayName = "Moodle",
	type = "moodle",
	position = NodePosition.START,
	settingsType = MoodleSettings::class,
	outputType = MoodleAssignment::class
)
class MoodleFetcher(
	override val nodeHandlerContext: NodeHandlerContext,
	private val moodleAPI: MoodleAPI = MoodleAPIImpl(nodeHandlerContext.httpClient())
) : AbstractIntegrationFetcher() {
	override val settingsClass: KClass<out NodeSettings> = MoodleSettings::class

	context(NodeHandlerContext, FetchContext)
	private suspend fun fetchAssignments(
		node: Node,
	): List<MoodleAssignment> {
		val moodleSettings = node.getConfig<MoodleSettings>()

		val assignments = fetchStage {
			moodleAPI.getCalendarUpcoming(moodleSettings)
		}

		updateStage {
			entityStateService.updateStates(node, assignments)
		}

		logger.info("Fetched ${assignments.size} assignments for ${moodleSettings.username}")

		return assignments
	}

	context(NodeHandlerContext, EmitNodeOutputContext)
	override suspend fun process(logger: Logger, node: Node, input: IntermediateData) {
		val jobContext: JobContext = input.get()
		val fetchContext = progress(jobContext, 1)

		iterableStructOutput(fetchContext) {
			return@iterableStructOutput fetchAssignments(node)
		}
	}
}
