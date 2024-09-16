package me.snoty.integration.moodle

import io.ktor.client.*
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.*
import me.snoty.integration.common.wiring.data.IntermediateData
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.moodle.model.MoodleAssignment
import me.snoty.integration.moodle.request.getCalendarUpcoming
import org.koin.core.annotation.Single
import org.slf4j.Logger
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
	val metadata: NodeMetadata,
	private val httpClient: HttpClient,
	private val moodleAPI: MoodleAPI = MoodleAPIImpl(httpClient)
) : NodeHandler {
	context(NodeHandleContext)
	override suspend fun process(
		logger: Logger,
		node: Node,
		input: Collection<IntermediateData>,
	) = input.flatMap { _ ->
		val moodleSettings = node.getConfig<MoodleSettings>()

		val assignments = moodleAPI.getCalendarUpcoming(moodleSettings)

		logger.atLevel(
			if (assignments.isEmpty()) Level.WARN
			else Level.INFO
		).log("Fetched ${assignments.size} assignments for ${moodleSettings.username}")

		iterableStructOutput(assignments)
	}
}
