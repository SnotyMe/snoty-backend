package me.snoty.backend.server.resources

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.integration.IntegrationManager
import me.snoty.backend.server.plugins.getUser

fun Application.integrationResources(integrationManager: IntegrationManager) = routing {
	route("integration") {
		get("list") {
			call.respond(HttpStatusCode.OK, integrationManager.integrations.map { it.name })
		}
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
