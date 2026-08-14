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
import me.snoty.core.NodeId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.http.invalidNodeId
import me.snoty.integration.common.model.metadata.NodeMetadata
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import org.koin.ktor.ext.get

suspend fun RoutingContext.getPersonalNodeOrNull(): StandaloneNode? {
	val nodeService: NodeService = get()

	val user = call.getUser()
	val id = call.parameters["id"]?.let(::NodeId)
		?: return void { call.invalidNodeId() }

	return nodeService.get(user.id, id) ?: void { call.invalidNodeId() }
}

@OptIn(InternalSerializationApi::class)
suspend fun RoutingContext.deserializeSettings(descriptor: NodeDescriptor, settingsJson: JsonElement): NodeSettings? {
	val nodeRegistry: NodeRegistry = get()
	val metadata = nodeRegistry.getMetadata(descriptor)

	val serializer = metadata.settingsClass.serializerOrNull()
		?: return void { noSerializerFound(metadata) }

	val json: Json = get()
	return json.decodeFromJsonElement(serializer, settingsJson)
}

suspend fun RoutingContext.noSerializerFound(metadata: NodeMetadata)
		= call.respond(HttpStatusCode.BadRequest, "No serializer found for ${metadata.settingsClass}")
