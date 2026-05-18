package me.snoty.integration.common.wiring.node

import io.ktor.http.*
import io.ktor.server.routing.*
import me.snoty.integration.common.wiring.Node

interface NodeRouteFactory {
	operator fun invoke(route: String, method: HttpMethod, verifyUser: Boolean = true, block: suspend RoutingContext.(Node) -> Unit)
}
