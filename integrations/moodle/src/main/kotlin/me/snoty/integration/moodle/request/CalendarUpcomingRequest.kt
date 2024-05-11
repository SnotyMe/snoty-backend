package me.snoty.backend.integration.moodle.request

import kotlinx.serialization.Serializable
import me.snoty.backend.integration.moodle.*
import me.snoty.integration.moodle.model.MoodleAssignment
import me.snoty.backend.integration.moodle.model.raw.MoodleEvent
import me.snoty.integration.moodle.model.toMoodleAssignment
import me.snoty.integration.moodle.MoodleAPI
import me.snoty.integration.moodle.MoodleRequest
import me.snoty.integration.moodle.MoodleSettings
import me.snoty.integration.moodle.request

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
