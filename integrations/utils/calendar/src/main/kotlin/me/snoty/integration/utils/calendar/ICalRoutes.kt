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
import me.snoty.integration.common.BaseIntegrationSettings
import me.snoty.backend.integration.config.ConfigId
import me.snoty.integration.common.config.IntegrationConfigService
import me.snoty.integration.common.config.get
import me.snoty.integration.common.utils.calendar.CalendarService
import net.fortuna.ical4j.data.CalendarOutputter
import java.nio.charset.StandardCharsets

@Serializable
data class ICalCreateRequest(val configId: ConfigId)

fun Route.calendarRoutes(
	integrationConfigService: IntegrationConfigService,
	calendarService: CalendarService,
	integrationType: String,
	calendarType: String,
	calendarBuilder: ICalBuilder<*>
) = route("/ical/$calendarType") {
	authenticate("jwt-auth") {
		post {
			val user = call.getUser()
			val configId = call.receive<ICalCreateRequest>().configId
			val settings = integrationConfigService.get<BaseIntegrationSettings>(configId, integrationType)
				?: throw NotFoundException("Integration config not found")
			val id = calendarService.create(
				user.id,
				settings.instanceId,
				integrationType,
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
		val storedCalendar = calendarService.get(ConfigId(calId), integrationType)
			?: throw NotFoundException("Calendar not found")
		val calendar = calendarBuilder.build(storedCalendar)
		val contentType = calendar.getContentType(StandardCharsets.UTF_8)
		val outputter = CalendarOutputter()
		call.respondOutputStream(ContentType.parse(contentType)) {
			outputter.output(calendar, this)
		}
	}
}
