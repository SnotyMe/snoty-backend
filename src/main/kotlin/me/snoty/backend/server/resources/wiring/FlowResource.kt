package me.snoty.backend.server.resources.wiring

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.logging.FlowExecutionService
import me.snoty.backend.scheduling.FlowJobRequest
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.server.koin.get
import me.snoty.backend.server.resources.wiring.flow.flowExportImportResource
import me.snoty.backend.server.resources.wiring.flow.getPersonalFlowOrNull
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.letOrNull
import me.snoty.backend.utils.orNull
import me.snoty.integration.common.http.flowNotFound
import me.snoty.integration.common.http.invalidNodeId
import me.snoty.integration.common.wiring.flow.FlowManagementService
import me.snoty.integration.common.wiring.flow.FlowService
import org.slf4j.event.Level

fun Route.flowResource() {
	val flowService: FlowService = get()
	val flowExecutionService: FlowExecutionService = get()

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
		val executions = flowExecutionService.query(user.id)
			.toList()

		call.respond(executions)
	}

	val flowScheduler: FlowScheduler = get()
	post("{id}/trigger") {
		val flow = getPersonalFlowOrNull() ?: return@post

		@Serializable
		data class FlowJobRequestRequest(val logLevel: Level)

		val jobRequest: FlowJobRequest =
			call.receiveNullable<FlowJobRequestRequest?>()
				?.let { any -> FlowJobRequest(logLevel = any.logLevel, triggeredBy = FlowTriggerReason.Manual) }
				?: FlowJobRequest(logLevel = Level.DEBUG, triggeredBy = FlowTriggerReason.Manual)

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

		val logs = flowExecutionService.retrieve(flowId = flow._id)

		call.respond(logs)
	}

	get("{id}/executions") {
		val startFrom = call.request.queryParameters["startFrom"]?.orNull()
		val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

		val flow = getPersonalFlowOrNull() ?: return@get

		val executions = flowExecutionService.query(flowId = flow._id, startFrom = startFrom, limit = limit)

		call.respond(executions)
	}

	val flowManagement: FlowManagementService = get()
	delete("{id}") {
		val flow = getPersonalFlowOrNull() ?: return@delete

		flowManagement.deleteFlowCascading(flow)

		call.respond(HttpStatusCode.OK)
	}

	flowExportImportResource()
}
