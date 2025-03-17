package me.snoty.integration.common.http

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.Node

suspend fun ApplicationCall.nodeNotFound(node: Node?) = nodeNotFound(node?._id)
suspend fun ApplicationCall.nodeNotFound(id: NodeId?) {
	val message = when {
		id != null -> "Node $id not found"
		else -> "Node not found"
	}
	respond(HttpStatusCode.NotFound, message)
}

suspend fun ApplicationCall.invalidNodeId() {
	respond(HttpStatusCode.BadRequest, "Invalid node id")
}
