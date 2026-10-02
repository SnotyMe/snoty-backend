package me.snoty.backend.server.resources.wiring.node

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import me.snoty.backend.scheduling.NodeScheduler
import me.snoty.backend.wiring.data.IntermediateData
import org.slf4j.event.Level

fun Route.nodeTrigger(nodeScheduler: NodeScheduler) {
    @Serializable
    data class NodeTriggerRequest(val logLevel: Level, val input: List<IntermediateData>)

    post("{id}/trigger") {
        val node = getPersonalNodeOrNull() ?: return@post

        val triggerRequest = call.receive<NodeTriggerRequest>()

        val result = nodeScheduler.trigger(node, logLevel = triggerRequest.logLevel, input = triggerRequest.input)
            ?: return@post call.respond(HttpStatusCode.Accepted)

        call.respond(HttpStatusCode.Accepted, result)
    }
}
