package me.snoty.integration.untis.request

import kotlinx.serialization.Serializable
import me.snoty.integration.untis.*
import me.snoty.integration.untis.helpers.toUserParams
import me.snoty.integration.untis.model.UntisDate
import me.snoty.integration.untis.model.UntisMasterData
import me.snoty.integration.untis.model.UntisTimetable
import me.snoty.integration.untis.param.TimetableParams
import java.time.LocalDate

suspend fun WebUntisAPI.getTimetable(userSettings: WebUntisSettings): TimetableResponse {
	val userData = getUserData(userSettings)

	val request = UntisRequest(userSettings) {
		data = UntisPayload {
			method = UntisApiConstants.Method.GET_TIMETABLE
			val userParams = userSettings.toUserParams()
			param(TimetableParams(
				id = userData.id,
				type = userData.type,
				// subtract one week to make sure to reach the latest monday
				// otherwise, the current week would be excluded
				startDate = UntisDate(LocalDate.now().minusWeeks(1)),
				endDate = UntisDate(LocalDate.now().plusWeeks(2)),
				auth = userParams.auth
			))
		}
	}

	return request<TimetableResponse>(request)
}

@Serializable
data class TimetableResponse(val timetable: UntisTimetable, val masterData: UntisMasterData)
