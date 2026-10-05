package me.snoty.backend.server.resources.wiring

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.Serializable
import me.snoty.backend.server.resources.wiring.flow.flowExportImportResource
import me.snoty.backend.server.resources.wiring.flow.getPersonalFlowOrNull
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.http.flowNotFound
import me.snoty.backend.utils.http.invalidFlowId
import me.snoty.backend.wiring.flow.FlowManagementService
import me.snoty.backend.wiring.flow.FlowService
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import me.snoty.core.flow.FlowId
import me.snoty.core.flow.WorkflowSettings
import org.koin.ktor.ext.get

@Serializable
data class FlowCreateRequest(val name: String, val settings: WorkflowSettings = WorkflowSettings())

fun Route.flowResource() = route("flow") {
	val flowService: FlowService = get()
	val flowExecutionService: FlowExecutionService = get()

	post {
		val user = call.getUser()
		val request = call.receive<FlowCreateRequest>()

		val flow = flowService.create(user.id, request.name, request.settings)

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

	get("{id}") {
		val user = call.getUser()
		val id = call.parameters["id"]?.let(::FlowId) ?: return@get call.invalidFlowId()

		val flow = flowService.getWithNodes(user.id, id) ?: return@get call.flowNotFound(id)

		call.respond(flow)
	}

	put("{id}/rename") {
		val flow = getPersonalFlowOrNull() ?: return@put

		val name = call.receiveText()
		flowService.rename(flow, name)

		call.respond(HttpStatusCode.NoContent)
	}.describe {
		// `receive<T>` is known, `receiveText` isn't
		// https://ktor.io/docs/openapi-spec-generation.html#code-inference
		requestBody = RequestBody(
			content = mapOf(
				ContentType.Text.Plain to MediaType(schema = ReferenceOr.value(JsonSchema(JsonType.STRING)))
			)
		)
	}

	put("{id}/settings") {
		val flow = getPersonalFlowOrNull() ?: return@put

		val settings = call.receive<WorkflowSettings>()
		flowService.updateSettings(flow, settings)

		call.respond(HttpStatusCode.NoContent)
	}

	val flowManagement: FlowManagementService = get()
	delete("{id}") {
		val flow = getPersonalFlowOrNull() ?: return@delete

		flowManagement.deleteFlowCascading(flow)

		call.respond(HttpStatusCode.OK)
	}

	flowExportImportResource()
}.describe {
	tag("flow")
}
