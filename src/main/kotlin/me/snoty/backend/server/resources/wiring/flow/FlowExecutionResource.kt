package me.snoty.backend.server.resources.wiring.flow

import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.json.Json
import me.snoty.backend.utils.orNull
import me.snoty.backend.wiring.flow.execution.FlowExecutionEventService
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import org.koin.ktor.ext.get
import kotlin.time.Duration.Companion.seconds

fun Route.flowExecutionResource() = route("{id}") {
	val flowExecutionService: FlowExecutionService = get()

	get("executions") {
		val startFrom = call.request.queryParameters["startFrom"]?.orNull()
		val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
		val flow = getPersonalFlowOrNull() ?: return@get
		val executions = flowExecutionService.query(flowId = flow._id, startFrom = startFrom, limit = limit)

		call.respond(executions)
	}

	val flowExecutionEventService: FlowExecutionEventService = get()
	val json: Json = get()
	
	sse("executions/sse") {
		val flow = getPersonalFlowOrNull() ?: return@sse

		heartbeat {
			period = 10.seconds
			event = ServerSentEvent("heartbeat")
		}

		val eventTypes = call.request.queryParameters["eventTypes"]?.split(",")?.map { it.trim() } ?: emptyList()

		flowExecutionEventService.provideBus()
			.filter { it.flowId == flow._id }
			.filter { eventTypes.isEmpty() || it.eventType in eventTypes }
			.collect {
				send(
					data = json.encodeToString(it),
					event = it.eventType,
				)
			}
	}
}
