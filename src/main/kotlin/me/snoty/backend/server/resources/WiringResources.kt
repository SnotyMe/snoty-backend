package me.snoty.backend.server.resources

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import me.snoty.backend.injection.ServicesContainer
import me.snoty.backend.server.resources.wiring.flowResource
import me.snoty.backend.server.resources.wiring.nodeResource

context(ServicesContainer)
fun Application.wiringResources(json: Json) = routing {
	authenticate("jwt-auth") {
		route("wiring") {
			route("node") {
				nodeResource(json)
			}

			route("flow") {
				flowResource()
			}
		}
	}
}
