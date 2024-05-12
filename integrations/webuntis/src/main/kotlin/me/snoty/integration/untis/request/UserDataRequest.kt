package me.snoty.integration.untis.request

import kotlinx.serialization.Serializable
import me.snoty.integration.untis.*
import me.snoty.integration.untis.helpers.toUserParams
import me.snoty.integration.untis.model.UntisUserData

suspend fun WebUntisAPI.getUserData(userSettings: WebUntisSettings): UntisUserData {
	val request = UntisRequest(userSettings) {
		data = UntisPayload {
			method = UntisApiConstants.Method.GET_USER_DATA
			param(userSettings.toUserParams())
		}
	}

	@Serializable
	data class UserDataResponse(val userData: UntisUserData)

	return request<UserDataResponse>(request).userData
}
