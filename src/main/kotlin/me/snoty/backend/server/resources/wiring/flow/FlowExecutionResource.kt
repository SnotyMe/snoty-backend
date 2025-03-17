package me.snoty.backend.server.resources.wiring.flow

import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.snoty.backend.utils.orNull
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import org.koin.ktor.ext.get

fun Route.flowExecutionResource() = route("{id}") {
	val flowExecutionService: FlowExecutionService = get()

	get("executions") {
		val startFrom = call.request.queryParameters["startFrom"]?.orNull()
		val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
		val flow = getPersonalFlowOrNull() ?: return@get
		val executions = flowExecutionService.query(flowId = flow._id, startFrom = startFrom, limit = limit)

		call.respond(executions)
	}
}