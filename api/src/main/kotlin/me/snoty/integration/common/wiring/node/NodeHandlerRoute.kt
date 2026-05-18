package me.snoty.integration.common.wiring.node

import io.ktor.http.*
import io.ktor.server.routing.*
import me.snoty.backend.config.Config

interface NodeHandlerRouteFactory {
	operator fun invoke(route: String, method: HttpMethod, authenticated: Boolean = true, block: suspend RoutingContext.() -> Unit)
}

fun buildHandlerNodeApiUrl(config: Config, descriptor: NodeDescriptor, route: String) =
	"${config.publicHost}/nodeapi/${descriptor.name}/$route"
