package me.snoty.backend.server.resources

import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.integration.moodle.MoodleAPI
import me.snoty.backend.integration.moodle.MoodleAPIImpl
import me.snoty.backend.integration.moodle.MoodleIntegration
import me.snoty.backend.integration.moodle.MoodleSettings
import me.snoty.backend.integration.moodle.request.getCalendarUpcoming
import me.snoty.backend.integration.moodle.request.getUser
import me.snoty.backend.server.handler.NotFoundException
import me.snoty.backend.server.plugins.getUser
import me.snoty.backend.utils.respondStatus

fun Route.moodleResources(moodleIntegration: MoodleIntegration, moodle: MoodleAPI = MoodleAPIImpl()) {
	post("userInfo") {
		val user = moodle.getUser(call.receive())
			?: return@post call.respondStatus(NotFoundException("User not found"))
		call.respond(user)
	}

	post("calendarUpcoming") {
		val settings = call.receive<MoodleSettings>()
		val assignments = moodle.getCalendarUpcoming(settings)
		call.respond(assignments)
	}

	post("schedule") {
		val settings = call.receive<MoodleSettings>()
		val user = call.getUser()
		moodleIntegration.schedule(user, settings)
	}
}
