package me.snoty.backend.server.resources.wiring

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import kotlinx.serialization.InternalSerializationApi
import me.snoty.backend.server.resources.wiring.node.nodeConnectionRoutes
import me.snoty.backend.server.resources.wiring.node.nodeCreate
import me.snoty.backend.server.resources.wiring.node.nodeDelete
import me.snoty.backend.server.resources.wiring.node.nodeUpdate
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.model.metadata.NodeMetadata
import org.koin.ktor.ext.get

@OptIn(InternalSerializationApi::class)
fun Route.nodeResource() = route("node") {
	val nodeService: NodeService = get()

	nodeCreate(nodeService)
	nodeDelete(nodeService)

	nodeConnectionRoutes(nodeService)

	nodeUpdate(nodeService)
}.describe {
	tag("node")
}

suspend fun RoutingContext.noSerializerFound(metadata: NodeMetadata)
	= call.respond(HttpStatusCode.BadRequest, "No serializer found for ${metadata.settingsClass}")
