package me.snoty.integration.common.wiring.node

import me.snoty.core.Node

interface NodeScopedPersistenceService {
	suspend fun delete(node: Node)
}
