package me.snoty.backend.server.resources

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.map
import me.snoty.backend.server.resources.wiring.nodeResource
import me.snoty.backend.utils.getUser
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeRegistry

fun Application.wiringResources(nodeRegistry: NodeRegistry, flowService: FlowService, nodeService: NodeService) = routing {
	authenticate("jwt-auth") {
		route("wiring") {
			route("node") {
				nodeResource(nodeRegistry, nodeService)
			}

			route("flow") {
				get("list") {
					val user = call.getUser()
					val flows = nodeService.getByUser(user.id, NodePosition.START).map {
						flowService.getFlowForNode(it)
					}

					call.respond(HttpStatusCode.OK, flows)
				}
			}
		}
	}
}
