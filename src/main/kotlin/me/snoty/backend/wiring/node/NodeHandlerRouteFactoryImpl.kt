package me.snoty.backend.wiring.node

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import me.snoty.backend.hooks.HookRegistry
import me.snoty.backend.hooks.impl.NodeapiRoutesHook
import me.snoty.backend.hooks.register
import me.snoty.backend.utils.UnauthorizedException
import me.snoty.backend.utils.getUserOrNull
import me.snoty.backend.utils.respondStatus
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeHandlerRouteFactory
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Provided

@Factory
internal class NodeHandlerRouteFactoryImpl(
    @Provided
    private val nodeDescriptor: NodeDescriptor,
    private val hookRegistry: HookRegistry,
) : NodeHandlerRouteFactory {
    val logger = KotlinLogging.logger {}

    /**
     * @param authenticated whether to verify that the user is authenticated
     */
    override operator fun invoke(route: String, method: HttpMethod, authenticated: Boolean, block: suspend RoutingContext.() -> Unit) =
        hookRegistry.register(NodeapiRoutesHook { routing ->
            logger.debug { "Registering route for ${nodeDescriptor.id} node handler: $route" }

            fun Route.doRoute() = route("${nodeDescriptor.name}/$route") {
                method(method) {
                    handle {
                        logger.debug { "Handling route for $nodeDescriptor node: $route" }

                        if (authenticated && call.getUserOrNull() == null) {
                            return@handle call.respondStatus(UnauthorizedException("User is not authenticated"))
                        }

                        block()
                    }
                }
            }

            if (authenticated) {
                routing.authenticate("jwt-auth") {
                    doRoute()
                }
            } else {
                routing.doRoute()
            }
        })
}
