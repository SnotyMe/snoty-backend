package me.snoty.backend.server.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.injection.ServicesContainer
import me.snoty.backend.server.resources.aboutResource
import me.snoty.backend.server.resources.wiringResources

fun Application.addResources(buildInfo: BuildInfo, servicesContainer: ServicesContainer, json: Json) = routing {
	with(servicesContainer) {
		aboutResource(buildInfo)
		wiringResources(json)
	}
}
