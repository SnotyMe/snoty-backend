package me.snoty.backend.server.resources

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.map
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializerOrNull
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.server.plugins.ktorJson
import me.snoty.backend.utils.getUser
import me.snoty.backend.utils.respondServiceResult
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeRegistry

@OptIn(InternalSerializationApi::class)
fun Application.wiringResources(nodeRegistry: NodeRegistry, flowService: FlowService, nodeService: NodeService) = routing {
	authenticate("jwt-auth") {
		route("wiring") {
			route("node") {
				get("list") {
					call.respond(HttpStatusCode.OK, nodeRegistry.getHandlers().keys)
				}
				post("create") {
					val user = call.getUser()
					@Serializable
					data class NodeCreateRequest(val descriptor: NodeDescriptor, val settings: JsonElement)
					val (descriptor, settingsJson) = call.receive<NodeCreateRequest>()
					val handler = nodeRegistry.lookupHandler(descriptor)
						?: return@post call.respond(HttpStatusCode.BadRequest, "No handler found for $descriptor")
					val serializer = handler.settingsClass.serializerOrNull()
						?: return@post call.respond(HttpStatusCode.BadRequest, "No serializer found for ${handler.settingsClass}")

					val settingsObj = ktorJson.decodeFromJsonElement(serializer, settingsJson)
					val id = nodeService.create(user.id, descriptor, settingsObj)

					call.respondText(status=HttpStatusCode.Created, text=id.toString())
				}
				put("connect") {
					@Serializable
					data class ConnectRequest(val from: NodeId, val to: NodeId)

					val (from, to) = call.receive<ConnectRequest>()

					val result = nodeService.connect(from, to)

					call.respondServiceResult(result)
				}
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
