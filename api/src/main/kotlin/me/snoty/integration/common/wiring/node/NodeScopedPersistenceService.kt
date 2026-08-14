package me.snoty.integration.common.wiring.node

import me.snoty.core.node.Node

interface NodeScopedPersistenceService {
	suspend fun delete(node: Node)
}
