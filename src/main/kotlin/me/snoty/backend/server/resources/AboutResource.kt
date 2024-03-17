package me.snoty.backend.server.resources

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.build.BuildInfo

fun Application.aboutResource(buildInfo: BuildInfo) = routing {
	get("/info") {
		call.respond(buildInfo)
	}
}
