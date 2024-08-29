package me.snoty.integration.common.wiring.node

import io.ktor.http.*
import io.ktor.server.routing.*
import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.hooks.impl.AddRoutesHook
import me.snoty.backend.hooks.registerHook
import me.snoty.backend.integration.config.flow.toNodeId
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.getUserOrNull
import me.snoty.backend.utils.respondStatus
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.http.nodeNotFound
import me.snoty.integration.common.wiring.Node
import org.koin.core.annotation.Factory

interface NodeRouteFactory {
	operator fun invoke(route: String, method: HttpMethod, verifyUser: Boolean = true, block: suspend RoutingContext.(Node) -> Unit)
}

@Factory
internal class NodeRouteFactoryImpl(
	private val nodeDescriptor: NodeDescriptor,
	private val hookRegistry: HookRegistry,
	private val nodeService: NodeService,
) : NodeRouteFactory {
	/**
	 * @param verifyUser whether to verify that the user is the owner of the node
	 */
	override operator fun invoke(route: String, method: HttpMethod, verifyUser: Boolean, block: suspend RoutingContext.(Node) -> Unit) =
		hookRegistry.registerHook<Routing, AddRoutesHook> { routing ->
			routing.route("{nodeId}/$route") {
				method(method) {
					handle {
						val nodeId = call.parameters["nodeId"]?.toNodeId()
							?: return@handle call.respondStatus(BadRequestException("nodeId is required"))
						val node = nodeService.get(nodeId)
							?: return@handle call.nodeNotFound(nodeId)

						if (node.descriptor != nodeDescriptor) {
							return@handle call.respondStatus(BadRequestException("This node is not a $nodeDescriptor node"))
						}

						if (verifyUser && node.userId != call.getUserOrNull()?.id) {
							return@handle call.nodeNotFound(nodeId)
						}

						block(this, node)
					}
				}
			}
		}
}
