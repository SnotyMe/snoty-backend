package me.snoty.backend.server.resources.wiring

import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import kotlinx.serialization.InternalSerializationApi
import me.snoty.backend.server.resources.wiring.node.nodeConnectionRoutes
import me.snoty.backend.server.resources.wiring.node.nodeCreate
import me.snoty.backend.server.resources.wiring.node.nodeDelete
import me.snoty.backend.server.resources.wiring.node.nodeUpdate
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.flow.FlowService
import org.koin.ktor.ext.get

@OptIn(InternalSerializationApi::class)
fun Route.nodeResource() = route("node") {
	val flowService: FlowService = get()
	val nodeService: NodeService = get()

	nodeCreate(flowService, nodeService)
	nodeDelete(nodeService)

	nodeConnectionRoutes(nodeService)

	nodeUpdate(nodeService)
}.describe {
	tag("node")
}
