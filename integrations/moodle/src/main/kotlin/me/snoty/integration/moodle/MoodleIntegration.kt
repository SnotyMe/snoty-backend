package me.snoty.integration.moodle

import io.ktor.client.*
import me.snoty.backend.utils.filterIfNot
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.iterableStructOutput
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.moodle.model.MoodleAssignment
import me.snoty.integration.moodle.model.MoodleAssignmentState
import me.snoty.integration.moodle.request.getCalendarUpcoming
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@RegisterNode(
	displayName = "Moodle",
	type = "moodle",
	position = NodePosition.START,
	settingsType = MoodleSettings::class,
	outputType = MoodleAssignment::class
)
@Single
class MoodleIntegration(
	httpClient: HttpClient,
	private val moodleAPI: MoodleAPI = MoodleAPIImpl(httpClient)
) : NodeHandler {
	override suspend fun NodeHandleContext.process(
		node: Node,
		input: Collection<IntermediateData>,
	) = input.flatMap { _ ->
		val moodleSettings = node.getConfig<MoodleSettings>()

		val assignments = moodleAPI.getCalendarUpcoming(moodleSettings)

		logger.atLevel(
			if (assignments.isEmpty()) Level.WARN
			else Level.INFO
		).log("Fetched ${assignments.size} assignments for ${moodleSettings.username}")

		iterableStructOutput(
			assignments
				.filterIfNot(moodleSettings.emitClosedAssignments) { it.state != MoodleAssignmentState.CLOSED }
				.filterIfNot(moodleSettings.emitDoneAssignments) { it.state != MoodleAssignmentState.DONE }
				.filterIfNot(moodleSettings.emitPastAssignments) { it.state != MoodleAssignmentState.PAST }
		)
	}
}
