package me.snoty.integration.common.wiring.node

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.hooks.impl.AddRoutesHook
import me.snoty.backend.hooks.register
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.getUserOrNull
import me.snoty.backend.utils.respondStatus
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.http.nodeNotFound
import me.snoty.integration.common.wiring.Node
import org.koin.core.annotation.Factory
import org.koin.ktor.ext.inject

interface NodeRouteFactory {
	operator fun invoke(route: String, method: HttpMethod, verifyUser: Boolean = true, block: suspend RoutingContext.(Node) -> Unit)
}

@Factory
internal class NodeRouteFactoryImpl(
	private val nodeDescriptor: NodeDescriptor,
	private val hookRegistry: HookRegistry,
) : NodeRouteFactory {
	val logger = KotlinLogging.logger {}

	/**
	 * @param verifyUser whether to verify that the user is the owner of the node
	 */
	override operator fun invoke(route: String, method: HttpMethod, verifyUser: Boolean, block: suspend RoutingContext.(Node) -> Unit) =
		hookRegistry.register(AddRoutesHook { routing ->
			logger.debug { "Registering route for $nodeDescriptor node: $route"}

			fun Route.doRoute() = route("{nodeId}/$route") {
				val nodeService: NodeService by inject()
				method(method) {
					handle {
						logger.debug { "Handling route for ${nodeDescriptor.id} nodes: $route"}

						val nodeId = call.parameters["nodeId"]
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


			if (verifyUser) {
				routing.authenticate("jwt-auth") {
					doRoute()
				}
			} else {
				routing.doRoute()
			}
		})
}
