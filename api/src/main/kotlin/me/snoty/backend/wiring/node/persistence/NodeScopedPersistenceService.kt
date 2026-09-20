package me.snoty.backend.wiring.node.persistence

import me.snoty.core.node.Node

interface NodeScopedPersistenceService {
	suspend fun delete(node: Node)
}
