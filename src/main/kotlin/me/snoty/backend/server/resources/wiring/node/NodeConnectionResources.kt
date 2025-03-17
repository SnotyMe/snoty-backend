package me.snoty.backend.server.resources.wiring.node

import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import kotlinx.serialization.Serializable
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.respondServiceResult
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.http.nodeNotFound

@Serializable
data class ConnectionRequest(val from: NodeId, val to: NodeId)

fun Route.connectionRoute(
	nodeService: NodeService,
	name: String,
	action: suspend NodeService.(from: NodeId, to: NodeId) -> ServiceResult,
) = put(name) {
	val user = call.getUser()

	val (from, to) = call.receive<ConnectionRequest>()
	val fromNode = nodeService.get(from)
	if (fromNode?.userId != user.id) {
		return@put call.nodeNotFound(fromNode)
	}
	val toNode = nodeService.get(to)
	if (toNode?.userId != user.id) {
		return@put call.nodeNotFound(toNode)
	}
	val result = nodeService.action(from, to)

	call.respondServiceResult(result)
}

fun Route.nodeConnectionRoutes(nodeService: NodeService) {
	connectionRoute(nodeService, "connect", NodeService::connect)
	connectionRoute(nodeService, "disconnect", NodeService::disconnect)
}
