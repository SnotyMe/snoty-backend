package me.snoty.integration.common.http

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.flow.Workflow

suspend fun ApplicationCall.flowNotFound(workflow: Workflow?) = flowNotFound(workflow?._id)
suspend fun ApplicationCall.flowNotFound(id: NodeId?) {
	val message = when {
		id != null -> "Flow $id not found"
		else -> "Flow not found"
	}
	respond(HttpStatusCode.NotFound, message)
}
