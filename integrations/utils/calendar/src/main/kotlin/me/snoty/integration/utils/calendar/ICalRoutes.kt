package me.snoty.integration.utils.calendar

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.NotFoundException
import me.snoty.backend.utils.getUser
import me.snoty.integration.common.IntegrationConfigTable
import me.snoty.integration.common.IntegrationSettings
import net.fortuna.ical4j.data.CalendarOutputter
import java.nio.charset.StandardCharsets
import java.util.*

@Serializable
data class ICalCreateRequest(val configId: Long)

fun Route.calendarRoutes(
	integrationName: String,
	type: String,
	calendarBuilder: ICalBuilder<*>
) = route("/ical/$type") {
	val calendarTable = CalendarTable(integrationName)

	authenticate("jwt-auth") {
		post {
			val user = call.getUser()
			val configId = call.receive<ICalCreateRequest>().configId
			val settings = IntegrationConfigTable.get<IntegrationSettings>(configId, integrationName)
				?: throw NotFoundException("Integration config not found")
			val id = calendarTable.create(
				user.id,
				settings.instanceId,
				type
			)

			call.response.header(HttpHeaders.Location, call.url {
				appendPathSegments("$id.ics")
			})
			call.respond(HttpStatusCode.Created, id.toString())
		}
	}
	get("{calId}.ics") {
		val calId = call.parameters["calId"] ?: throw BadRequestException("Invalid calendar ID")
		// calendar stored in DB
		// created by a user before adding to their client to bypass authentication
		val storedCalendar = calendarTable.get(UUID.fromString(calId), type)
			?: throw NotFoundException("Calendar not found")
		val config = CalendarConfig(storedCalendar[calendarTable.userId], storedCalendar[calendarTable.instanceId], type)
		val calendar = calendarBuilder.build(config)
		val contentType = calendar.getContentType(StandardCharsets.UTF_8)
		val outputter = CalendarOutputter()
		call.respondOutputStream(ContentType.parse(contentType)) {
			outputter.output(calendar, this)
		}
	}
}
