package me.snoty.backend.server.resources.wiring.node

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.letOrNull
import me.snoty.backend.utils.respondServiceResult
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.http.nodeNotFound
import me.snoty.integration.common.wiring.node.NodeDescriptor

fun Route.nodeCreate(nodeService: NodeService) = post("create") {
	val user = call.getUser()

	@Serializable
	data class NodeCreateRequest(
		val flowId: NodeId,
		val descriptor: NodeDescriptor,
		val settings: JsonElement,
	)

	val (flowId, descriptor, settingsJson) = call.receive<NodeCreateRequest>()
	val settingsObj = deserializeSettings(descriptor, settingsJson) ?: return@post
	val createdNode = nodeService.create(user.id, flowId, descriptor, settingsObj)

	call.respond(status = HttpStatusCode.Created, message = createdNode)
}

fun Route.nodeDelete(nodeService: NodeService) = delete("{id}") {
	val user = call.getUser()

	val id = call.parameters["id"]?.letOrNull { NodeId(it) }
		?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid node id")

	val node = nodeService.get(id)
	if (node?.userId != user.id) {
		return@delete nodeNotFound(node)
	}

	val result = nodeService.delete(id)

	call.respondServiceResult(result)
}
