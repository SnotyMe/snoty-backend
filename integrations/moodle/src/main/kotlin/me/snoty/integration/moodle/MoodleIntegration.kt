package me.snoty.integration.moodle

import io.ktor.client.*
import me.snoty.backend.notifications.NotificationAttributes
import me.snoty.backend.notifications.NotificationService
import me.snoty.backend.utils.filterIfNot
import me.snoty.backend.wiring.credential.resolveOrNull
import me.snoty.integration.common.annotation.RegisterNode
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContext
import me.snoty.integration.common.wiring.data.NodeInput
import me.snoty.integration.common.wiring.data.NodeOutput
import me.snoty.integration.common.wiring.data.iterableStructOutput
import me.snoty.integration.common.wiring.getConfig
import me.snoty.integration.common.wiring.logger
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
	context(_: NodeHandleContext)
	override suspend fun process(
		node: Node,
		input: NodeInput,
	): NodeOutput {
		val userId = node.userId.toString()

		val moodleSettings = node.getConfig<MoodleSettings>()
		val credentials = moodleSettings.credentials.resolveOrNull(userId) ?: throw MoodleCredentialsMissingException()

		val authFailureAttributes = NotificationAttributes(type = "moodle.authfailure", flowId = node.flowId, nodeId = node._id)
		val assignments = try {
			moodleAPI.getCalendarUpcoming(moodleSettings, credentials)
		} catch (e: MoodleException) {
			when (e) {
				is MoodleInvalidTokenException -> {
					val message = "Failed to authenticate with Moodle. Please check your credentials. They may have expired."
					logger.warn(message)
					notificationService.send(
						userId = userId,
						title = "Moodle Authentication Error",
						description = message,
						attributes = authFailureAttributes,
					)
					return emptyList()
				}
				else -> throw e
			}
		}
		notificationService.resolve(userId, authFailureAttributes)

		logger.atLevel(
			if (assignments.isEmpty()) Level.WARN
			else Level.INFO
		).log("Fetched ${assignments.size} assignments for ${credentials.username}")

		return iterableStructOutput(
			assignments
				.filterIfNot(moodleSettings.emitClosedAssignments) { it.state != MoodleAssignmentState.CLOSED }
				.filterIfNot(moodleSettings.emitDoneAssignments) { it.state != MoodleAssignmentState.DONE }
				.filterIfNot(moodleSettings.emitPastAssignments) { it.state != MoodleAssignmentState.PAST }
		)
	}
}
