package me.snoty.backend.integration.untis.request

import kotlinx.serialization.Serializable
import me.snoty.backend.integration.untis.*
import me.snoty.backend.integration.untis.model.UntisDate
import me.snoty.backend.integration.untis.model.UntisTimetable
import me.snoty.backend.integration.untis.param.UntisAuth
import me.snoty.backend.integration.untis.param.UntisParam

suspend fun WebUntisAPI.getTimeTable(userSettings: WebUntisSettings, query: TimetableParams): TimeTableResponse {
	val request = UntisRequest(userSettings) {
		data = UntisPayload {
			method = UntisApiConstants.Method.GET_TIMETABLE
			param(query)
		}
	}

	return request<TimeTableResponse>(request)
}

@Serializable
data class TimeTableResponse(val timetable: UntisTimetable)

@Serializable
data class TimetableParams(
	val id: Int,
	val type: String,
	val startDate: UntisDate,
	val endDate: UntisDate,
	val masterDataTimestamp: Long = 0,
	val timetableTimestamp: Long = 0,
	val timetableTimestamps: List<Long> = listOf(),
	val auth: UntisAuth
) : UntisParam()
