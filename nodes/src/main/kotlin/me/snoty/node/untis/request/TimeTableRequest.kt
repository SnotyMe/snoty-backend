package me.snoty.node.untis.request

import kotlinx.serialization.Serializable
import me.snoty.node.untis.*
import me.snoty.node.untis.helpers.toUserParams
import me.snoty.node.untis.model.UntisDate
import me.snoty.node.untis.model.UntisMasterData
import me.snoty.node.untis.model.UntisTimetable
import me.snoty.node.untis.param.TimetableParams
import java.time.LocalDate

suspend fun WebUntisAPI.getTimetable(userSettings: WebUntisSettings): TimetableResponse {
	val userData = getUserData(userSettings)

	val request = UntisRequest(
		userSettings,
		UntisPayload {
			method = UntisApiConstants.Method.GET_TIMETABLE
			val userParams = userSettings.toUserParams()
			param(
				TimetableParams(
					id = userData.id,
					type = userData.type,
					// subtract one week to make sure to reach the latest monday
					// otherwise, the current week would be excluded
					startDate = UntisDate(LocalDate.now().minusWeeks(1)),
					endDate = UntisDate(LocalDate.now().plusWeeks(2)),
					auth = userParams.auth
				)
			)
		},
	)

	return request<TimetableResponse>(request)
}

@Serializable
data class TimetableResponse(val timetable: UntisTimetable, val masterData: UntisMasterData)
