package me.snoty.backend.server.resources.wiring

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.InternalSerializationApi
import me.snoty.backend.server.koin.get
import me.snoty.backend.server.resources.wiring.node.nodeConnectionRoutes
import me.snoty.backend.server.resources.wiring.node.nodeCreate
import me.snoty.backend.server.resources.wiring.node.nodeDelete
import me.snoty.backend.server.resources.wiring.node.nodeUpdate
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.model.metadata.NodeMetadata

@OptIn(InternalSerializationApi::class)
fun Route.nodeResource() {
	val nodeService: NodeService = get()

	nodeCreate(nodeService)
	nodeDelete(nodeService)

	nodeConnectionRoutes(nodeService)

	nodeUpdate(nodeService)
}

suspend fun RoutingContext.noSerializerFound(metadata: NodeMetadata)
	= call.respond(HttpStatusCode.BadRequest, "No serializer found for ${metadata.settingsClass}")
