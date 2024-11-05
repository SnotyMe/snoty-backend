package me.snoty.integration.common.wiring.node

import me.snoty.integration.common.wiring.Node

interface NodeScopedPersistenceService {
	suspend fun delete(node: Node)
}
