package me.snoty.backend.server.resources.wiring.node

import io.ktor.server.routing.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializerOrNull
import me.snoty.backend.server.plugins.void
import me.snoty.backend.server.resources.wiring.noSerializerFound
import me.snoty.backend.utils.getUser
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.http.invalidNodeId
import me.snoty.integration.common.http.nodeNotFound
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import org.koin.ktor.ext.get

suspend fun RoutingContext.getPersonalNodeOrNull(): StandaloneNode? {
	val nodeService: NodeService = get()

	val user = call.getUser()
	val id = call.parameters["id"]
		?: return void { call.invalidNodeId() }

	val flow = nodeService.get(id)
	if (flow?.userId != user.id) {
		call.nodeNotFound(flow)
		return null
	}

	return flow
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
