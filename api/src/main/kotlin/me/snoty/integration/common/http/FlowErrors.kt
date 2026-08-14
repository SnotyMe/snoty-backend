package me.snoty.integration.common.http

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import me.snoty.core.flow.FlowId

suspend fun ApplicationCall.flowNotFound(flowId: FlowId?) {
	val message = when {
		flowId != null -> "Flow $flowId not found"
		else -> "Flow not found"
	}
	respond(HttpStatusCode.NotFound, message)
}

suspend fun ApplicationCall.invalidFlowId() {
	respond(HttpStatusCode.BadRequest, "Invalid flow id")
}
