package me.snoty.backend.server.resources

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.server.resources.wiring.flowResource
import me.snoty.backend.server.resources.wiring.nodeMetadataResource
import me.snoty.backend.server.resources.wiring.nodeResource
import me.snoty.backend.server.routing.Resource
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named("wiring")
fun wiringResources(json: Json, hookRegistry: HookRegistry) = Resource {
	route("wiring/node") {
		hookRegistry.executeHooks(Route::class, this)

		route("metadata") {
			nodeMetadataResource()
		}
	}

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
