package me.snoty.backend.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.integration.IntegrationManager
import me.snoty.backend.server.resources.aboutResource
import me.snoty.backend.server.resources.untisResources

fun Application.addResources(buildInfo: BuildInfo, integrationManager: IntegrationManager) = routing {
	aboutResource(buildInfo)
	route("integration") {
		route("untis") { untisResources() }
		integrationManager.integrations.forEach { integration ->
			route(integration.name) {
				authenticate("jwt-auth") {
					post("schedule") {
						val user = call.getUser()
						val settings = call.receive(integration.settingsType)
						integration.schedule(user, settings)
						call.respond(HttpStatusCode.OK)
					}
				}
			}
		}
	}
}
