package me.snoty.backend.server.resources.wiring.node

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.respondServiceResult
import me.snoty.core.flow.FlowId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.http.flowNotFound
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition

fun Route.nodeCreate(flowService: FlowService, nodeService: NodeService) = post("create") {
	val user = call.getUser()

	@Serializable
	data class NodeCreateRequest(
		val flowId: FlowId,
		val descriptor: NodeDescriptor,
		val position: NodePosition,
		val settings: JsonElement,
	)

	val (requestedFlowId, descriptor, position, settingsJson) = call.receive<NodeCreateRequest>()
	val flow = flowService.getStandalone(user.id, requestedFlowId) ?: return@post call.flowNotFound(requestedFlowId)
	val settingsObj = deserializeSettings(descriptor, settingsJson) ?: return@post
	val createdNode = nodeService.create(user.id, flow, descriptor, position, settingsObj)

	call.respond(status = HttpStatusCode.Created, message = createdNode)
}

fun Route.nodeDelete(nodeService: NodeService) = delete("{id}") {
	val node = getPersonalNodeOrNull() ?: return@delete

	val result = nodeService.delete(node)

	call.respondServiceResult(result)
}
