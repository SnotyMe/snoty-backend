package me.snoty.backend.wiring.node.state

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.wiring.node.persistence.NodeScopedPersistenceService
import me.snoty.core.node.Node
import org.bson.Document

interface EntityStateService : NodeScopedPersistenceService {
	suspend fun getLastState(node: Node, entityId: String): EntityState?
	fun getLastStates(node: Node): Flow<EntityState>

	suspend fun updateState(node: Node, state: Document, diff: DiffResult)
	suspend fun updateStates(node: Node, states: Collection<EntityStateUpdate>)

	data class EntityStateUpdate(val state: EntityState, val diffResult: DiffResult)
}
