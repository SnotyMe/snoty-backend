package me.snoty.node.untis.request

import kotlinx.serialization.Serializable
import me.snoty.node.untis.*
import me.snoty.node.untis.helpers.toUserParams
import me.snoty.node.untis.model.UntisMasterData
import me.snoty.node.untis.model.UntisUserData

suspend fun WebUntisAPI.getUserData(userSettings: WebUntisSettings): UntisUserData
	= getUserAndMasterData(userSettings).userData

@Serializable
data class UserDataResponse(val userData: UntisUserData, val masterData: UntisMasterData)

suspend fun WebUntisAPI.getUserAndMasterData(userSettings: WebUntisSettings): UserDataResponse {
	val request = UntisRequest(
		userSettings,
		UntisPayload {
			method = UntisApiConstants.Method.GET_USER_DATA
			param(userSettings.toUserParams())
		},
	)

	return request<UserDataResponse>(request)
}
