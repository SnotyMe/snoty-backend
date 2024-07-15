package me.snoty.backend.server.resources.wiring

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import me.snoty.backend.injection.ServicesContainer
import me.snoty.backend.injection.get
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.letOrNull
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeRegistry

context(ServicesContainer)
fun Route.flowResource() {
	val nodeRegistry = get<NodeRegistry>()
	val nodeService = get<NodeService>()
	val flowService = get<FlowService>()

	get("list") {
		val user = call.getUser()
		val flows = nodeService.getByUser(user.id, NodePosition.START)
		val result = flows.toList()
		call.respond(result)
	}

	get("{id}") {
		val user = call.getUser()
		val id = call.parameters["id"]?.letOrNull { NodeId(it) }
			?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid node id")

		val node = nodeService.get(id)
		if (node?.userId != user.id) {
			return@get call.nodeNotFound(node)
		}
		val handler = nodeRegistry.lookupHandler(node.descriptor) ?: return@get call.noHandlerFound(node.descriptor)
		if (handler.position != NodePosition.START) {
			return@get call.respond(HttpStatusCode.BadRequest, "Node is not a flow start node")
		}

		val flow = flowService.getFlowForNode(node)
			.single()

		call.respond(flow)
	}
}
