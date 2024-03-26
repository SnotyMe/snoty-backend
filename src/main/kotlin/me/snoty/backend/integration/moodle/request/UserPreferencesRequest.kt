package me.snoty.backend.integration.moodle.request

import me.snoty.backend.integration.moodle.*
import me.snoty.backend.integration.moodle.model.MoodleUser
import me.snoty.backend.integration.moodle.param.UserByFieldMoodleParam

suspend fun MoodleAPI.getUser(userSettings: MoodleSettings): MoodleUser? {
	val request = MoodleRequest(userSettings) {
		method = MoodleApiConstants.Function.Core.User.GET_BY_FIELD
		param(UserByFieldMoodleParam("username", userSettings.username))
	}

	return request<List<MoodleUser>>(request)
		.firstOrNull()
}
