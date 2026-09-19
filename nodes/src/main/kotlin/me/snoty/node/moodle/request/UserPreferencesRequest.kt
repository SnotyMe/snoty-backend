package me.snoty.node.moodle.request

import me.snoty.node.moodle.*
import me.snoty.node.moodle.model.MoodleUser
import me.snoty.node.moodle.param.UserByFieldMoodleParam

suspend fun MoodleAPI.getUser(credential: MoodleCredential): MoodleUser? {
	val request = MoodleRequest(credential) {
		method = MoodleApiConstants.Function.Core.User.GET_BY_FIELD
		param(UserByFieldMoodleParam("username", credential.username))
	}

	return request<List<MoodleUser>>(request)
		.firstOrNull()
}
