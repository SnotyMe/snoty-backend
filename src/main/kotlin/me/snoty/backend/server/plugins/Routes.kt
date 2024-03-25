package me.snoty.backend.server.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.server.resources.aboutResource
import me.snoty.backend.server.resources.untisResources

fun Application.addResources(buildInfo: BuildInfo) = routing {
	aboutResource(buildInfo)
	route("integration") {
		route("untis") { untisResources() }
	}
}
