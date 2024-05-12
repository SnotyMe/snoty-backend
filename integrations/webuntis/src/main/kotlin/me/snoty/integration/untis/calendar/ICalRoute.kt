package me.snoty.integration.untis.calendar

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.snoty.backend.utils.NotFoundException
import me.snoty.backend.utils.getUser
import me.snoty.integration.common.IntegrationConfigTable
import me.snoty.integration.untis.WebUntisIntegration
import me.snoty.integration.untis.WebUntisSettings
import me.snoty.integration.untis.model.UntisExam
import net.fortuna.ical4j.data.CalendarOutputter
import java.nio.charset.StandardCharsets
import java.util.*

@Serializable
data class ICalCreateRequest(val configId: Long)

fun Route.iCalRoute() = route("/ical") {
	authenticate("jwt-auth") {
		post("/exams/create") {
			val user = call.getUser()
			val configId = call.receive<ICalCreateRequest>().configId
			val integrationConfig = IntegrationConfigTable.get<WebUntisSettings>(configId, WebUntisIntegration.INTEGRATION_NAME)
				?: throw NotFoundException("Integration config not found")

			val id = WebUntisCalendar.create(
				user.id,
				integrationConfig.instanceId,
				UntisExam.TYPE
			)

			call.respond(HttpStatusCode.Created, id.toString())
		}
	}
	get("/exams/{calId}.ics") {
		val calId = call.parameters["calId"] ?: return@get call.respondText("Invalid calendar ID", status = HttpStatusCode.BadRequest)
		// calendar stored in DB
		// created by a user before adding to their client to bypass authentication
		val storedCalendar = WebUntisCalendar.get(UUID.fromString(calId), UntisExam.TYPE)
			?: return@get call.respondText("Calendar not found", status = HttpStatusCode.NotFound)
		val config = CalendarConfig(storedCalendar[WebUntisCalendar.userId], storedCalendar[WebUntisCalendar.instanceId], UntisExam.TYPE)
		val calendar = ICalBuilder.build(config)

		val contentType = calendar.getContentType(StandardCharsets.UTF_8)

		val outputter = CalendarOutputter()
		call.respondOutputStream(ContentType.parse(contentType)) {
			outputter.output(calendar, this)
		}
	}
}
