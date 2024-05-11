package me.snoty.integration.moodle.request

import me.snoty.integration.moodle.*
import me.snoty.integration.moodle.model.MoodleUser
import me.snoty.integration.moodle.param.UserByFieldMoodleParam

suspend fun MoodleAPI.getUser(userSettings: MoodleSettings): MoodleUser? {
	val request = MoodleRequest(userSettings) {
		method = MoodleApiConstants.Function.Core.User.GET_BY_FIELD
		param(UserByFieldMoodleParam("username", userSettings.username))
	}

	return request<List<MoodleUser>>(request)
		.firstOrNull()
}
