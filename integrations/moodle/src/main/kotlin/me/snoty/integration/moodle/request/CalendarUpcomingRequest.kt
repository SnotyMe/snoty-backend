package me.snoty.integration.moodle.request

import kotlinx.serialization.Serializable
import me.snoty.integration.moodle.*
import me.snoty.integration.moodle.model.MoodleAssignment
import me.snoty.integration.moodle.model.raw.MoodleEvent
import me.snoty.integration.moodle.model.toMoodleAssignment

@Serializable
data class CalendarUpcomingResponse(val events: List<MoodleEvent>)

suspend fun MoodleAPI.getCalendarUpcoming(credential: MoodleCredential): List<MoodleAssignment> {
	val request = MoodleRequest(credential) {
		method = MoodleApiConstants.Function.Core.Calendar.GET_UPCOMING_VIEW
	}

	return request<CalendarUpcomingResponse>(request)
		.events
		.map { it.toMoodleAssignment() }
}
