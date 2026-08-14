package me.snoty.backend.server.resources.wiring.node

import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.respondServiceResult
import me.snoty.core.Node
import me.snoty.core.NodeId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.http.nodeNotFound

@Serializable
data class ConnectionRequest(val from: NodeId, val to: NodeId)

fun Route.connectionRoute(
	nodeService: NodeService,
	name: String,
	action: suspend NodeService.(from: Node, to: Node) -> ServiceResult,
) = put(name) {
	val user = call.getUser()

	val (requestedFrom, requestedTo) = call.receive<ConnectionRequest>()
	val fromNode = nodeService.get(user.id, requestedFrom) ?: return@put call.nodeNotFound(requestedFrom)
	val toNode = nodeService.get(user.id, requestedTo) ?: return@put call.nodeNotFound(requestedTo)

	val result = nodeService.action(fromNode, toNode)

	call.respondServiceResult(result)
}

fun Route.nodeConnectionRoutes(nodeService: NodeService) {
	connectionRoute(nodeService, "connect", NodeService::connect)
	connectionRoute(nodeService, "disconnect", NodeService::disconnect)
}
