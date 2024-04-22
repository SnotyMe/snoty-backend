package me.snoty.backend.server.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.server.resources.aboutResource
import me.snoty.backend.server.resources.moodleResources
import me.snoty.backend.server.resources.untisResources
import org.jetbrains.exposed.sql.Database

fun Application.addResources(buildInfo: BuildInfo, database: Database) = routing {
	aboutResource(buildInfo)
	route("integration") {
		route("untis") { untisResources() }
		route("moodle") { moodleResources(database) }
	}
}
