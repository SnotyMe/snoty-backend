package me.snoty.backend.server.resources

import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.build.BuildInfo

fun Route.aboutResource(buildInfo: BuildInfo) {
	get("/info") {
		call.respond(buildInfo)
	}
}
