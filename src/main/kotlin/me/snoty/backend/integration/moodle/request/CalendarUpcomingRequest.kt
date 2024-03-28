package me.snoty.backend.integration.moodle.request

import kotlinx.serialization.Serializable
import me.snoty.backend.integration.moodle.*
import me.snoty.backend.integration.moodle.model.MoodleAssignment
import me.snoty.backend.integration.moodle.model.raw.MoodleEvent
import me.snoty.backend.integration.moodle.model.toMoodleAssignment

suspend fun MoodleAPI.getCalendarUpcoming(userSettings: MoodleSettings): List<MoodleAssignment> {
	val request = MoodleRequest(userSettings) {
		method = MoodleApiConstants.Function.Core.Calendar.GET_UPCOMING_VIEW
	}

	@Serializable
	data class Response(val events: List<MoodleEvent>)

	return request<Response>(request)
		.events
		.map { it.toMoodleAssignment() }
}
