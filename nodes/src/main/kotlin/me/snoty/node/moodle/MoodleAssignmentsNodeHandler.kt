package me.snoty.node.moodle

import me.snoty.backend.notifications.NotificationAttributes
import me.snoty.backend.notifications.NotificationService
import me.snoty.backend.utils.filterIfNot
import me.snoty.backend.wiring.credential.resolve
import me.snoty.backend.wiring.data.NodeInput
import me.snoty.backend.wiring.data.NodeOutput
import me.snoty.backend.wiring.data.iterableStructOutput
import me.snoty.backend.wiring.node.*
import me.snoty.backend.wiring.node.metadata.NodeStereotype
import me.snoty.core.node.NodeWithSettings
import me.snoty.core.node.getConfig
import me.snoty.node.moodle.model.MoodleAssignment
import me.snoty.node.moodle.model.MoodleAssignmentState
import me.snoty.node.moodle.request.getCalendarUpcoming
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@RegisterNode(
	name = "moodle_assignments",
	displayName = "Moodle",
	icon = Icon(name = "devicon-moodle"),
	stereotype = NodeStereotype.START,
	settingsType = MoodleSettings::class,
	outputType = MoodleAssignment::class
)
@Single
class MoodleAssignmentsNodeHandler(
	private val notificationService: NotificationService,
	private val moodleAPI: MoodleAPI,
) : NodeHandler {
	context(_: NodeHandleContext)
	override suspend fun process(
		node: NodeWithSettings,
		input: NodeInput,
	): NodeOutput {
		val moodleSettings = node.getConfig<MoodleSettings>()
		val credentials = moodleSettings.credentials.resolve(node.userId)

		val authFailureAttributes = NotificationAttributes(type = "moodle.authfailure", flowId = node.flowId, nodeId = node.id)
		val assignments = try {
			moodleAPI.getCalendarUpcoming(credentials)
		} catch (e: MoodleException) {
			when (e) {
				is MoodleInvalidTokenException -> {
					val message = "Failed to authenticate with Moodle. Please check your credentials. They may have expired."
					logger.warn(message)
					notificationService.send(
						userId = node.userId,
						title = "Moodle Authentication Error",
						description = message,
						attributes = authFailureAttributes,
					)
					return emptyList()
				}
				else -> throw e
			}
		}
		notificationService.resolve(node.userId, authFailureAttributes)

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
