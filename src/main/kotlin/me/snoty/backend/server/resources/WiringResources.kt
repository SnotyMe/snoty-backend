package me.snoty.backend.server.resources

import io.ktor.server.auth.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.hooks.impl.NodeapiRoutesHook
import me.snoty.backend.server.resources.wiring.flow.flowExecutionResource
import me.snoty.backend.server.resources.wiring.flowResource
import me.snoty.backend.server.resources.wiring.nodeMetadataResource
import me.snoty.backend.server.resources.wiring.nodeResource
import me.snoty.backend.server.routing.Resource
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
@Named("wiring")
fun wiringResources(hookRegistry: HookRegistry) = Resource {
	route("wiring") {
		route("node") {
			route("metadata") {
				nodeMetadataResource()
			}
		}
		
		route("nodeapi") innerRoute@{
			runBlocking {
				hookRegistry.executeHooks(NodeapiRoutesHook::class, this@innerRoute)
			}
		}
	}

	authenticate("jwt-auth") {
		route("wiring") {
			route("node") {
				nodeResource()
			}

			route("flow") {
				flowResource()
				flowExecutionResource()
			}
		}
	}
}
