package me.snoty.backend.server.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.server.resources.aboutResource
import me.snoty.backend.server.resources.wiringResources
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodeRegistry

fun Application.addResources(buildInfo: BuildInfo, nodeRegistry: NodeRegistry, flowService: FlowService, nodeService: NodeService) = routing {
	aboutResource(buildInfo)
	wiringResources(nodeRegistry, flowService, nodeService)
}
