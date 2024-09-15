package me.snoty.integration.common.diff

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.diff.state.EntityState
import org.bson.Document

interface EntityStateService {
	suspend fun getLastState(nodeId: NodeId, entityId: String): EntityState?

	suspend fun updateState(nodeId: NodeId, state: Document, diff: DiffResult)
}
