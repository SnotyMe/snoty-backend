package me.snoty.integration.common.wiring.node

import io.ktor.http.*
import io.ktor.server.routing.*
import me.snoty.backend.hooks.impl.AddRoutesHook
import me.snoty.backend.integration.config.flow.toNodeId
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.getUserOrNull
import me.snoty.backend.utils.respondStatus
import me.snoty.integration.common.http.nodeNotFound
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.addHook

/**
 * @param verifyUser whether to verify that the user is the owner of the node
 */
fun NodeHandler.nodeRoute(
	name: String,
	method: HttpMethod,
	verifyUser: Boolean = true,
	block: suspend RoutingContext.(Node) -> Unit
) = nodeHandlerContext.addHook<Routing, AddRoutesHook> { routing ->
	routing.route("{nodeId}/$name") {
		method(method) {
			handle {
				val nodeId = call.parameters["nodeId"]?.toNodeId()
					?: return@handle call.respondStatus(BadRequestException("nodeId is required"))

				val node = nodeHandlerContext.nodeService.get(nodeId)
					?: return@handle call.nodeNotFound(nodeId)

				if (verifyUser && node.userId != call.getUserOrNull()?.id) {
					return@handle call.nodeNotFound(nodeId)
				}

				block(this, node)
			}
		}
	}
}
