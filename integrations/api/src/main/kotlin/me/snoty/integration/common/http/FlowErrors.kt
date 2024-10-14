package me.snoty.integration.common.http

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.flow.Workflow

suspend fun RoutingContext.flowNotFound(workflow: Workflow?) = flowNotFound(workflow?._id)
suspend fun RoutingContext.flowNotFound(id: NodeId?) {
	val message = when {
		id != null -> "Flow $id not found"
		else -> "Flow not found"
	}
	call.respond(HttpStatusCode.NotFound, message)
}
