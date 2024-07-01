package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.NodeContext

interface NodeHandlerContributor {
	fun contributeHandlers(registry: NodeRegistry, context: NodeContext)
}
