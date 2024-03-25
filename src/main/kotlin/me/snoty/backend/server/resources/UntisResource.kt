package me.snoty.backend.server.resources

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.snoty.backend.integration.untis.WebUntisAPI
import me.snoty.backend.integration.untis.WebUntisAPIImpl
import me.snoty.backend.integration.untis.WebUntisSettings
import me.snoty.backend.integration.untis.helpers.toUserParams
import me.snoty.backend.integration.untis.model.UntisDate
import me.snoty.backend.integration.untis.request.TimetableParams
import me.snoty.backend.integration.untis.request.getTimeTable
import me.snoty.backend.integration.untis.request.getUserData

fun Route.untisResources(untis: WebUntisAPI = WebUntisAPIImpl()) {
	post("userInfo") {
		call.respond(untis.getUserData(call.receive()))
	}

	post("timeTable") {
		@Serializable
		data class TimetableQuery(
			val id: Int,
			val type: String,
			val startDate: UntisDate,
			val endDate: UntisDate
		)

		@Serializable
		data class TimeTableRequest(val settings: WebUntisSettings, val query: TimetableQuery)
		val request = call.receive<TimeTableRequest>()

		val timeTableRequest = request.query.run {
			TimetableParams(
				id = id,
				type = type,
				startDate = startDate,
				endDate = endDate,
				auth = request.settings.toUserParams().auth
			)
		}

		call.respond(untis.getTimeTable(request.settings, timeTableRequest))
	}
}
