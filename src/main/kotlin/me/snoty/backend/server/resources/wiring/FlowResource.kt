package me.snoty.backend.server.resources.wiring

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.logging.FlowLogService
import me.snoty.backend.server.koin.get
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.letOrNull
import me.snoty.integration.common.http.flowNotFound
import me.snoty.integration.common.wiring.flow.FlowService

fun Routing.flowResource() {
	val flowService: FlowService = get()

	get("list") {
		val user = call.getUser()
		val flows = flowService.query(user.id)
		val result = flows.toList()

		call.respond(result)
	}

	get("{id}") {
		val user = call.getUser()
		val id = call.parameters["id"]?.letOrNull { NodeId(it) }
			?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid node id")

		val flow = flowService.getWithNodes(id)
		if (flow?.userId != user.id) {
			return@get call.flowNotFound(flow)
		}

		call.respond(flow)
	}

	val flowLogService = get<FlowLogService>()
	get("{id}/logs") {
		val user = call.getUser()

		val id = call.parameters["id"]?.letOrNull { NodeId(it) }
			?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid node id")
		val flow = flowService.getStandalone(id)
		if (flow?.userId != user.id) {
			return@get call.flowNotFound(flow)
		}

		val logs = flowLogService.retrieve(flowId = id)

		call.respond(logs)
	}
}
