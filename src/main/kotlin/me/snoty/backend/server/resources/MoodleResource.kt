package me.snoty.backend.server.resources

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.integration.moodle.MoodleAPI
import me.snoty.backend.integration.moodle.MoodleAPIImpl
import me.snoty.backend.integration.moodle.request.getCalendarUpcoming
import me.snoty.backend.integration.moodle.request.getUser
import me.snoty.backend.server.handler.NotFoundException
import me.snoty.backend.utils.respondStatus

fun Route.moodleResources(moodle: MoodleAPI = MoodleAPIImpl()) {
	post("userInfo") {
		val user = moodle.getUser(call.receive())
			?: return@post call.respondStatus(NotFoundException("User not found"))
		call.respond(user)
	}

	post("calendarUpcoming") {
		call.respond(moodle.getCalendarUpcoming(call.receive()))
	}
}
