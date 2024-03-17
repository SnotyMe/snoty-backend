package me.snoty.backend.server.plugins

import io.ktor.server.application.*
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.server.resources.aboutResource

fun Application.addResources(buildInfo: BuildInfo) {
	aboutResource(buildInfo)
}
