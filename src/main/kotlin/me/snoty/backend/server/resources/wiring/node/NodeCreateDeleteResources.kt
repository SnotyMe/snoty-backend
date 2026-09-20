package me.snoty.backend.server.resources.wiring.node

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.http.flowNotFound
import me.snoty.backend.utils.respondServiceResult
import me.snoty.backend.wiring.flow.FlowService
import me.snoty.backend.wiring.node.NodePosition
import me.snoty.backend.wiring.node.NodeService
import me.snoty.core.flow.FlowId
import me.snoty.core.node.NodeType

@Serializable
private data class NodeCreateRequest(
	val flowId: FlowId,
	val type: NodeType,
	val name: String,
	val position: NodePosition,
	val settings: JsonElement,
)

fun Route.nodeCreate(flowService: FlowService, nodeService: NodeService) = post("create") {
	val user = call.getUser()

	val (requestedFlowId, type, name, position, settingsJson) = call.receive<NodeCreateRequest>()
	val flow = flowService.getStandalone(user.id, requestedFlowId) ?: return@post call.flowNotFound(requestedFlowId)
	val settingsObj = deserializeSettings(type, settingsJson) ?: return@post
	val createdNode = nodeService.create(user.id, flow, type, name, position, settingsObj)

	call.respond(status = HttpStatusCode.Created, message = createdNode)
}

fun Route.nodeDelete(nodeService: NodeService) = delete("{id}") {
	val node = getPersonalNodeOrNull() ?: return@delete

	val result = nodeService.delete(node)

	call.respondServiceResult(result)
}
