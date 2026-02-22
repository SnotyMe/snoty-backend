package me.snoty.backend.server.resources.wiring.flow

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import me.snoty.backend.server.plugins.void
import me.snoty.backend.utils.getUser
import me.snoty.core.FlowId
import me.snoty.integration.common.http.flowNotFound
import me.snoty.integration.common.http.invalidFlowId
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.flow.StandaloneWorkflow
import org.koin.ktor.ext.get

// necessary because for some reason `ServerSSESession` does not implement `RoutingContext`

suspend fun ServerSSESession.getPersonalFlowOrNull() = call.getPersonalFlowOrNull()

suspend fun RoutingContext.getPersonalFlowOrNull() = call.getPersonalFlowOrNull()

suspend fun ApplicationCall.getPersonalFlowOrNull(): StandaloneWorkflow? {
	val user = getUser()
	val id = parameters["id"] ?: return void { invalidFlowId() }

	val flow = get<FlowService>().getStandalone(FlowId(id))
	if (flow?.userId != user.id) {
		return void { flowNotFound(flow) }
	}

	return flow
}
