package me.snoty.backend.wiring.node

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.hooks.impl.NodeapiRoutesHook
import me.snoty.backend.hooks.register
import me.snoty.backend.utils.BadRequestException
import me.snoty.backend.utils.getUserOrNull
import me.snoty.backend.utils.http.nodeNotFound
import me.snoty.backend.utils.respondStatus
import me.snoty.backend.wiring.node.routing.NodeRouteFactory
import me.snoty.core.node.NodeId
import me.snoty.core.node.NodeType
import me.snoty.core.node.NodeWithSettings
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided
import org.koin.ktor.ext.inject

@Factory
internal class NodeRouteFactoryImpl(
	@Provided
	private val nodeType: NodeType,
	private val hookRegistry: HookRegistry,
) : NodeRouteFactory {
	val logger = KotlinLogging.logger {}

	/**
	 * @param verifyUser whether to verify that the user is the owner of the node
	 */
	override operator fun invoke(route: String, method: HttpMethod, verifyUser: Boolean, block: suspend RoutingContext.(NodeWithSettings) -> Unit) =
		hookRegistry.register(NodeapiRoutesHook { routing ->
			logger.debug { "Registering route for $nodeType node: $route" }

			fun Route.doRoute() = route("${nodeType.value}/{nodeId}/$route") {
				val nodeService: NodeService by inject()
				method(method) {
					handle {
						logger.debug { "Handling route for $nodeType node: $route" }

						val userId = when {
							verifyUser -> call.getUserOrNull()?.id
								?: return@handle call.respondStatus(BadRequestException("User is not authenticated"))

							else -> null
						}

						val nodeId = call.parameters["nodeId"]?.let(::NodeId)
							?: return@handle call.respondStatus(BadRequestException("nodeId is required"))
						val node = nodeService.get(userId, nodeId)
							?: return@handle call.nodeNotFound(nodeId)

						if (node.type != nodeType) {
							return@handle call.respondStatus(BadRequestException("This node is not a $nodeType"))
						}

						block(this, node)
					}
				}.describe {
					tag("node:${nodeType.value}")
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
