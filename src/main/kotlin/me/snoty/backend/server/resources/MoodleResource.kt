package me.snoty.backend.server.resources

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.integration.moodle.MoodleAPI
import me.snoty.backend.integration.moodle.MoodleAPIImpl
import me.snoty.backend.integration.moodle.MoodleEntityStateTable
import me.snoty.backend.integration.moodle.MoodleSettings
import me.snoty.backend.integration.moodle.request.getCalendarUpcoming
import me.snoty.backend.integration.moodle.request.getUser
import me.snoty.backend.server.handler.NotFoundException
import me.snoty.backend.utils.respondStatus
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.moodleResources(database: Database, moodle: MoodleAPI = MoodleAPIImpl()) {
	post("userInfo") {
		val user = moodle.getUser(call.receive())
			?: return@post call.respondStatus(NotFoundException("User not found"))
		call.respond(user)
	}

	post("calendarUpcoming") {
		val settings = call.receive<MoodleSettings>()
		val assignments = moodle.getCalendarUpcoming(settings)
		assignments.forEach {
			transaction(database) {
				MoodleEntityStateTable.compareAndUpdateState(settings.baseUrl.hashCode(), it)
			}
		}
		call.respond(assignments)
	}
}
