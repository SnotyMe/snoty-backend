package me.snoty.backend.server.resources.wiring

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializerOrNull
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.server.koin.get
import me.snoty.backend.server.plugins.void
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.letOrNull
import me.snoty.backend.utils.respondServiceResult
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.http.nodeNotFound
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings

@OptIn(InternalSerializationApi::class)
fun Route.nodeResource(json: Json) {
	val nodeRegistry: NodeRegistry = get()
	val nodeService: NodeService = get()

	suspend fun deserializeSettings(call: ApplicationCall, descriptor: NodeDescriptor, settingsJson: JsonElement): NodeSettings? {
		val metadata = nodeRegistry.getMetadata(descriptor)
		val serializer = metadata.settingsClass.serializerOrNull()
			?: return void { call.noSerializerFound(metadata) }
		return json.decodeFromJsonElement(serializer, settingsJson)
	}

	post("create") {
		val user = call.getUser()

		@Serializable
		data class NodeCreateRequest(
			val flowId: NodeId,
			val descriptor: NodeDescriptor,
			val settings: JsonElement,
		)

		val (flowId, descriptor, settingsJson) = call.receive<NodeCreateRequest>()
		val settingsObj = deserializeSettings(call, descriptor, settingsJson) ?: return@post

		val createdNode = nodeService.create(user.id, flowId, descriptor, settingsObj)

		call.respond(status = HttpStatusCode.Created, message = createdNode)
	}

	@Serializable
	data class ConnectionRequest(val from: NodeId, val to: NodeId)

	fun connectionRoute(name: String, action: suspend NodeService.(from: NodeId, to: NodeId) -> ServiceResult) = put(name) {
		val user = call.getUser()

		val (from, to) = call.receive<ConnectionRequest>()
		val fromNode = nodeService.get(from)
		if (fromNode?.userId != user.id) {
			return@put call.nodeNotFound(fromNode)
		}
		val toNode = nodeService.get(to)
		if (toNode?.userId != user.id) {
			return@put call.nodeNotFound(toNode)
		}
		val result = nodeService.action(from, to)

		call.respondServiceResult(result)
	}

	connectionRoute("connect", NodeService::connect)
	connectionRoute("disconnect", NodeService::disconnect)

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

	delete("{id}") {
		val user = call.getUser()

		val id = call.parameters["id"]?.letOrNull { NodeId(it) }
			?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid node id")

		val node = nodeService.get(id)
		if (node?.userId != user.id) {
			return@delete call.nodeNotFound(node)
		}

		val result = nodeService.delete(id)

		call.respondServiceResult(result)
	}
}

suspend fun ApplicationCall.noHandlerFound(descriptor: NodeDescriptor)
	= respond(HttpStatusCode.BadRequest, "No handler found for $descriptor")

suspend fun ApplicationCall.noSerializerFound(metadata: NodeMetadata)
	= respond(HttpStatusCode.BadRequest, "No serializer found for ${metadata.settingsClass}")
