package me.snoty.backend.server.resources

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.build.BuildInfo
import org.koin.ktor.ext.inject

fun Application.aboutResource() = routing {
	val buildInfo by inject<BuildInfo>()

	get("/info") {
		call.respond(buildInfo)
	}
}
