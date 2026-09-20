package me.snoty.backend.wiring.node.routing

import io.ktor.http.*
import io.ktor.server.routing.*
import me.snoty.backend.config.Config
import me.snoty.core.node.NodeType

interface NodeHandlerRouteFactory {
	operator fun invoke(route: String, method: HttpMethod, authenticated: Boolean = true, block: suspend RoutingContext.() -> Unit)
}

fun buildHandlerNodeApiUrl(config: Config, type: NodeType, route: String) =
	"${config.publicHost}/nodeapi/${type.value}/$route"
