package me.snoty.integration.moodle

import io.ktor.client.*
import me.snoty.backend.notifications.NotificationAttributes
import me.snoty.backend.notifications.NotificationService
import me.snoty.backend.utils.filterIfNot
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.iterableStructOutput
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.moodle.model.MoodleAssignment
import me.snoty.integration.moodle.model.MoodleAssignmentState
import me.snoty.integration.moodle.request.getCalendarUpcoming
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@RegisterNode(
	name = "moodle_assignments",
	displayName = "Moodle",
	position = NodePosition.START,
	settingsType = MoodleSettings::class,
	outputType = MoodleAssignment::class
)
@Single
class MoodleIntegration(
	httpClient: HttpClient,
	private val notificationService: NotificationService,
	private val moodleAPI: MoodleAPI = MoodleAPIImpl(httpClient),
) : NodeHandler {
	override suspend fun NodeHandleContext.process(
		node: Node,
		input: NodeInput,
	): NodeOutput {
		val moodleSettings = node.getConfig<MoodleSettings>()

		val assignments = try {
			moodleAPI.getCalendarUpcoming(moodleSettings)
		} catch (e: MoodleException) {
			when (e) {
				is MoodleInvalidTokenException -> {
					val message = "Failed to authenticate with Moodle. Please check your credentials. They may have expired."
					logger.warn(message)
					notificationService.send(
						userId = node.userId.toString(),
						title = "Moodle Authentication Error",
						description = message,
						attributes = NotificationAttributes(type = "moodle.authfailure", flowId = node.flowId, nodeId = node._id),
					)
					return emptyList()
				}
				else -> throw e
			}
		}

		logger.atLevel(
			if (assignments.isEmpty()) Level.WARN
			else Level.INFO
		).log("Fetched ${assignments.size} assignments for ${moodleSettings.username}")

		return iterableStructOutput(
			assignments
				.filterIfNot(moodleSettings.emitClosedAssignments) { it.state != MoodleAssignmentState.CLOSED }
				.filterIfNot(moodleSettings.emitDoneAssignments) { it.state != MoodleAssignmentState.DONE }
				.filterIfNot(moodleSettings.emitPastAssignments) { it.state != MoodleAssignmentState.PAST }
		)
	}
}
