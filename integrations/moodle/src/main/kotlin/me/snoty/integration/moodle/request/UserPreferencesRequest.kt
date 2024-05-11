package me.snoty.integration.moodle.request

import me.snoty.backend.integration.moodle.*
import me.snoty.backend.integration.moodle.model.MoodleUser
import me.snoty.backend.integration.moodle.param.UserByFieldMoodleParam
import me.snoty.integration.moodle.MoodleAPI
import me.snoty.integration.moodle.MoodleRequest
import me.snoty.integration.moodle.MoodleSettings
import me.snoty.integration.moodle.request

suspend fun MoodleAPI.getUser(userSettings: MoodleSettings): MoodleUser? {
	val request = MoodleRequest(userSettings) {
		method = MoodleApiConstants.Function.Core.User.GET_BY_FIELD
		param(UserByFieldMoodleParam("username", userSettings.username))
	}

	return request<List<MoodleUser>>(request)
		.firstOrNull()
}
