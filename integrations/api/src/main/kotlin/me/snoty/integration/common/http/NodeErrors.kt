package me.snoty.integration.common.http

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.Node

suspend fun RoutingContext.nodeNotFound(node: Node?) = nodeNotFound(node?._id)
suspend fun RoutingContext.nodeNotFound(id: NodeId?) {
	val message = when {
		id != null -> "Node $id not found"
		else -> "Node not found"
	}
	call.respond(HttpStatusCode.NotFound, message)
}

suspend fun RoutingContext.invalidNodeId() {
	call.respond(HttpStatusCode.BadRequest, "Invalid node id")
}
