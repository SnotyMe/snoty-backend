package me.snoty.integration.moodle

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import me.snoty.backend.test.IntermediateDataMapperRegistry
import me.snoty.backend.utils.bson.getIdAsString
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.NodeHandleContextImpl
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.moodle.model.raw.MoodleAction
import me.snoty.integration.moodle.model.raw.MoodleCourse
import me.snoty.integration.moodle.model.raw.MoodleEvent
import me.snoty.integration.moodle.request.CalendarUpcomingResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.concurrent.ThreadLocalRandom
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MoodleIntegrationTest {
	fun integration(moodleAPI: MoodleAPI) = MoodleIntegration(
		httpClient = mockk(),
		notificationService = mockk(relaxed = true),
		moodleAPI = moodleAPI,
	)

	fun randomLong() = ThreadLocalRandom.current().nextLong()

	@OptIn(ExperimentalUuidApi::class)
	fun randomString() = Uuid.random().toString()

	fun assignment(id: Long, due: Instant, overdue: Boolean, action: MoodleAction? = null): MoodleEvent {
		return MoodleEvent(
			id = id,
			instance = id,
			name = "Assignment $id",
			description = "Description $id",
			timeStart = due.epochSeconds,
			overdue = overdue,
			action = action,
			course = MoodleCourse(
				id = randomLong(),
				fullname = "Course $id",
				shortname = "C$id",
			),
		)
	}

	val baseSettings = MoodleSettings(
		baseUrl = randomString(),
		username = randomString(),
		appSecret = randomString()
	)

	@TestFactory
	fun `test filters`() = listOf(
		baseSettings.copy(emitDoneAssignments = false, emitClosedAssignments = false, emitPastAssignments = false) to listOf(1, 2),
		baseSettings.copy(emitDoneAssignments = true, emitClosedAssignments = false, emitPastAssignments = false) to listOf(1, 2, 10),
		baseSettings.copy(emitDoneAssignments = false, emitClosedAssignments = true, emitPastAssignments = false) to listOf(1, 2, 11),
		baseSettings.copy(emitDoneAssignments = false, emitClosedAssignments = false, emitPastAssignments = true) to listOf(1, 2, 12),
		baseSettings.copy(emitDoneAssignments = true, emitClosedAssignments = true, emitPastAssignments = true) to listOf(1, 2, 10, 11, 12),
	).map { (settings, shouldPass) ->
		val submitAction = MoodleAction(name = "submit", url = "", actionable = true)

		val dueEvent = assignment(1, Clock.System.now().plus(1.hours), overdue = false, action = submitAction)
		// will still be emitted because it has an action
		val overdueEvent = assignment(2, Clock.System.now().minus(5.minutes), overdue = true, action = submitAction)

		val doneEvent = assignment(10, Clock.System.now().minus(5.minutes), overdue = false, action = null)
		val closedEvent = assignment(11, Clock.System.now().minus(1.hours), overdue = false, action = MoodleAction(name = "close", url = "", actionable = false))
		val pastEvent = assignment(12, Clock.System.now().minus(1.hours), overdue = true, action = null)

		DynamicTest.dynamicTest("$shouldPass should pass with $settings") {
			runBlocking {
				val moodleAPI = mockk<MoodleAPI>()

				coEvery { moodleAPI.request<CalendarUpcomingResponse>(any(), any()) } returns CalendarUpcomingResponse(
					events = listOf(
						doneEvent,
						overdueEvent,
						dueEvent,
						closedEvent,
						pastEvent,
					)
				)

				val integration = integration(moodleAPI)
				val output = with(integration) {
					val ctx = NodeHandleContextImpl(
						intermediateDataMapperRegistry = IntermediateDataMapperRegistry,
						logger = mockk(relaxed = true),
					)
					val node: Node = mockk(relaxed = true)
					every { node.settings } returns settings

					ctx.process(node, listOf(mockk()))
				}
				assertEquals(shouldPass.sorted(), output.map { (it as BsonIntermediateData).value.getIdAsString()!!.toInt() }.sorted())
			}
		}
	}
}
