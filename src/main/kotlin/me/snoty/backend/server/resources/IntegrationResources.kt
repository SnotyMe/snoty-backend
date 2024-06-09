package me.snoty.backend.server.resources

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.snoty.backend.integration.IntegrationManager
import me.snoty.backend.utils.NotFoundException
import me.snoty.backend.utils.getUser
import me.snoty.integration.common.config.ConfigId
import me.snoty.integration.common.name

fun Application.integrationResources(integrationManager: IntegrationManager) = routing {
	route("integration") {
		get("list") {
			call.respond(HttpStatusCode.OK, integrationManager.integrations.map { it.name })
		}
		integrationManager.integrations.forEach { integration ->
			route(integration.name) {
				authenticate("jwt-auth") {
					post("setup") {
						val user = call.getUser()
						val settings = call.receive(integration.settingsType)
						val configId = integration.setup(user, settings)
						call.respondText(configId.toHexString(), status = HttpStatusCode.Created)
					}

					post("schedule") {
						@Serializable
						data class ScheduleRequest(val configId: ConfigId)
						val user = call.getUser()
						val scheduleRequest = call.receive<ScheduleRequest>()
						val configId = scheduleRequest.configId

						val settings = integrationManager.getIntegrationConfig(configId, integration.name)
							?: throw NotFoundException("Integration config not found")

						integration.schedule(user, settings)

						call.respond(HttpStatusCode.NoContent)
					}
				}
				// adds any extra routes from
				integration.routes(this)
			}
		}
	}
}
