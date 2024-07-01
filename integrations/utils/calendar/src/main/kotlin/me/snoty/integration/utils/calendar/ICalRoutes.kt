package me.snoty.integration.utils.calendar

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.NotFoundException
import me.snoty.backend.utils.getUser
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.utils.calendar.CalendarId
import me.snoty.integration.common.utils.calendar.CalendarService
import net.fortuna.ical4j.data.CalendarOutputter
import java.nio.charset.StandardCharsets

@Serializable
data class ICalCreateRequest(val nodeId: NodeId)

fun Route.calendarRoutes(
	nodeService: NodeService,
	calendarService: CalendarService,
	calendarType: String,
	calendarBuilder: ICalBuilder<*>
) = route("/ical/$calendarType") {
	authenticate("jwt-auth") {
		post {
			call.getUser()

			val nodeId = call.receive<ICalCreateRequest>().nodeId
			// check if node even exists
			nodeService.get(nodeId)
				?: throw NotFoundException("Node not found")
			val id = calendarService.create(
				nodeId,
				calendarType
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

		val storedCalendarId = calendarService.get(CalendarId(calId))
			?: throw NotFoundException("Calendar not found")
		val node = nodeService.get(storedCalendarId)
			?: throw NotFoundException("Node not found")

		val calendar = calendarBuilder.build(node)
		val contentType = calendar.getContentType(StandardCharsets.UTF_8)
		val outputter = CalendarOutputter()
		call.respondOutputStream(ContentType.parse(contentType)) {
			outputter.output(calendar, this)
		}
	}
}
