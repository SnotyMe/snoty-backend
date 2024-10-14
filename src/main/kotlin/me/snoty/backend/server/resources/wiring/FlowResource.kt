package me.snoty.backend.server.resources.wiring

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.logging.FlowLogService
import me.snoty.backend.scheduling.FlowJobRequest
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.server.koin.get
import me.snoty.backend.server.plugins.void
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.letOrNull
import me.snoty.integration.common.http.flowNotFound
import me.snoty.integration.common.http.invalidNodeId
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.flow.StandaloneWorkflow
import org.slf4j.event.Level

fun Route.flowResource() {
	val flowService: FlowService = get()
	val flowLogService: FlowLogService = get()

	post {
		val user = call.getUser()

		@Serializable
		data class FlowCreateRequest(val name: String)
		val request = call.receive<FlowCreateRequest>()

		val flow = flowService.create(user.id, request.name)

		call.respond(flow)
	}

	get("list") {
		val user = call.getUser()
		val flows = flowService.query(user.id)
		val result = flows.toList()

		call.respond(result)
	}

	get("list/executions") {
		val user = call.getUser()
		val executions = flowLogService.query(user.id)
			.toList()

		call.respond(executions)
	}

	suspend fun RoutingContext.getPersonalFlowOrNull(): StandaloneWorkflow? {
		val user = call.getUser()
		val id = call.parameters["id"]?.letOrNull { NodeId(it) }
			?: return void { invalidNodeId() }

		val flow = flowService.getStandalone(id)
		if (flow?.userId != user.id) {
			return void { flowNotFound(flow) }
		}

		return flow
	}

	val flowScheduler: FlowScheduler = get()
	post("{id}/trigger") {
		val flow = getPersonalFlowOrNull() ?: return@post

		val jobRequest: FlowJobRequest = call.receiveNullable() ?: FlowJobRequest(logLevel = Level.DEBUG)

		flowScheduler.trigger(flow, jobRequest)

		call.respond(HttpStatusCode.Accepted)
	}

	get("{id}") {
		val user = call.getUser()
		val id = call.parameters["id"]?.letOrNull { NodeId(it) }
			?: return@get invalidNodeId()

		val flow = flowService.getWithNodes(id)
		if (flow?.userId != user.id) {
			return@get flowNotFound(flow)
		}

		call.respond(flow)
	}

	put("{id}/rename") {
		val flow = getPersonalFlowOrNull() ?: return@put

		val name = call.receiveText()
		flowService.rename(flow._id, name)

		call.respond(HttpStatusCode.NoContent)
	}

	get("{id}/logs") {
		val flow = getPersonalFlowOrNull() ?: return@get

		val logs = flowLogService.retrieve(flowId = flow._id)

		call.respond(logs)
	}
}
