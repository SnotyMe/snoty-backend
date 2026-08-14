package me.snoty.integration.common.diff

import kotlinx.coroutines.flow.Flow
import me.snoty.core.Node
import me.snoty.integration.common.diff.state.EntityState
import me.snoty.integration.common.wiring.node.NodeScopedPersistenceService
import org.bson.Document

interface EntityStateService : NodeScopedPersistenceService {
	suspend fun getLastState(node: Node, entityId: String): EntityState?
	fun getLastStates(node: Node): Flow<EntityState>

	suspend fun updateState(node: Node, state: Document, diff: DiffResult)
	suspend fun updateStates(node: Node, states: Collection<EntityStateUpdate>)

	data class EntityStateUpdate(val state: EntityState, val diffResult: DiffResult)
}
