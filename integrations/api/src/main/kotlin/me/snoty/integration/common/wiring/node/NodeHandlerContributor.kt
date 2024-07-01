package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.NodeContextBuilder

interface NodeHandlerContributor {
	fun contributeHandlers(registry: NodeRegistry, nodeContextBuilder: NodeContextBuilder)
}
