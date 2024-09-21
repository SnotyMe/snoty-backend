package me.snoty.integration.common.diff

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.diff.state.EntityState
import org.bson.Document

interface EntityStateService {
	suspend fun getLastState(nodeId: NodeId, entityId: String): EntityState?
	fun getLastStates(nodeId: NodeId): Flow<EntityState>

	suspend fun updateState(nodeId: NodeId, state: Document, diff: DiffResult)
	suspend fun updateStates(nodeId: NodeId, states: Collection<EntityStateUpdate>)

	data class EntityStateUpdate(val state: EntityState, val diffResult: DiffResult)
}
