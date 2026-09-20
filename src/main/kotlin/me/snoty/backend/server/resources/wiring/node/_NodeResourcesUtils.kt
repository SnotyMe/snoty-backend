package me.snoty.backend.server.resources.wiring.node

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializerOrNull
import me.snoty.backend.server.plugins.void
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.http.invalidNodeId
import me.snoty.backend.utils.http.nodeNotFound
import me.snoty.backend.wiring.node.NodeService
import me.snoty.backend.wiring.node.NodeSettings
import me.snoty.backend.wiring.node.metadata.NodeMetadata
import me.snoty.backend.wiring.node.registry.NodeRegistry
import me.snoty.core.node.NodeId
import me.snoty.core.node.NodeType
import me.snoty.core.node.StandaloneNode
import org.koin.ktor.ext.get

suspend fun RoutingContext.getPersonalNodeOrNull(): StandaloneNode? {
	val nodeService: NodeService = get()

	val user = call.getUser()
	val id = call.parameters["id"]?.let(::NodeId)
		?: return void { call.invalidNodeId() }

	return nodeService.get(user.id, id) ?: void { call.nodeNotFound(id) }
}

@OptIn(InternalSerializationApi::class)
suspend fun RoutingContext.deserializeSettings(nodeType: NodeType, settingsJson: JsonElement): NodeSettings? {
	val nodeRegistry: NodeRegistry = get()
	val metadata = nodeRegistry.getMetadata(nodeType)

	val serializer = metadata.settingsClass.serializerOrNull()
		?: return void { noSerializerFound(metadata) }

	val json: Json = get()
	return json.decodeFromJsonElement(serializer, settingsJson)
}

suspend fun RoutingContext.noSerializerFound(metadata: NodeMetadata)
		= call.respond(HttpStatusCode.BadRequest, "No serializer found for ${metadata.settingsClass}")
