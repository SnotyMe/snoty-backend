package me.snoty.backend.server.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.integration.IntegrationManager
import me.snoty.backend.server.resources.aboutResource
import me.snoty.backend.server.resources.integrationResources

fun Application.addResources(buildInfo: BuildInfo, integrationManager: IntegrationManager) = routing {
	aboutResource(buildInfo)
	integrationResources(integrationManager)
}
