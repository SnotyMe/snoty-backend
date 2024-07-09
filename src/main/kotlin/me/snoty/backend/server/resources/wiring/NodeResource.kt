package me.snoty.backend.server.resources.wiring

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializerOrNull
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.server.plugins.void
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.letOrNull
import me.snoty.backend.utils.respondServiceResult
import me.snoty.integration.common.SnotyJson
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeHandler
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings

@OptIn(InternalSerializationApi::class)
fun Route.nodeResource(nodeRegistry: NodeRegistry, nodeService: NodeService) {
	suspend fun deserializeSettings(call: ApplicationCall, descriptor: NodeDescriptor, settingsJson: JsonElement): NodeSettings? {
		val handler = nodeRegistry.lookupHandler(descriptor)
			?: return void { call.noHandlerFound(descriptor) }
		val serializer = handler.settingsClass.serializerOrNull()
			?: return void { call.noSerializerFound(handler) }
		val settingsObj = SnotyJson.decodeFromJsonElement(serializer, settingsJson)
		return settingsObj
	}

	get("list") {
		call.respond(HttpStatusCode.OK, nodeRegistry.getHandlers().keys)
	}
	post("create") {
		val user = call.getUser()

		@Serializable
		data class NodeCreateRequest(
			val descriptor: NodeDescriptor,
			val settings: JsonElement
		)

		val (descriptor, settingsJson) = call.receive<NodeCreateRequest>()
		val settingsObj = deserializeSettings(call, descriptor, settingsJson) ?: return@post

		val id = nodeService.create(user.id, descriptor, settingsObj)

		call.respondText(status = HttpStatusCode.Created, text = id.toString())
	}
	put("connect") {
		val user = call.getUser()

		@Serializable
		data class ConnectRequest(val from: NodeId, val to: NodeId)

		val (from, to) = call.receive<ConnectRequest>()
		val fromNode = nodeService.get(from)
		if (fromNode?.userId != user.id) {
			return@put call.nodeNotFound(fromNode)
		}
		val toNode = nodeService.get(to)
		if (toNode?.userId != user.id) {
			return@put call.nodeNotFound(toNode)
		}
		val result = nodeService.connect(from, to)

		call.respondServiceResult(result)
	}
	put("{id}") {
		val user = call.getUser()

		val id = call.parameters["id"]?.letOrNull { NodeId(it) }
			?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid node id")
		val node = nodeService.get(id)
		if (node?.userId != user.id) {
			return@put call.nodeNotFound(node)
		}

		val settingsJson = call.receive<JsonElement>()
		val settingsObj = deserializeSettings(call, node.descriptor, settingsJson) ?: return@put

		val result = nodeService.updateSettings(id, settingsObj)

		call.respondServiceResult(result)
	}
}

private suspend fun ApplicationCall.noHandlerFound(descriptor: NodeDescriptor)
	= respond(HttpStatusCode.BadRequest, "No handler found for $descriptor")

private suspend fun ApplicationCall.noSerializerFound(handler: NodeHandler)
	= respond(HttpStatusCode.BadRequest, "No serializer found for ${handler.settingsClass}")

suspend fun ApplicationCall.nodeNotFound(node: IFlowNode?) = nodeNotFound(node?._id)
suspend fun ApplicationCall.nodeNotFound(id: NodeId?) {
	val message = when {
		id != null -> "Node $id not found"
		else -> "Node not found"
	}
	respond(HttpStatusCode.NotFound, message)
}
