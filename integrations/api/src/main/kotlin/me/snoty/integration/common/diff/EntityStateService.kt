package me.snoty.integration.common.diff

import kotlinx.coroutines.flow.Flow
import me.snoty.integration.common.diff.state.EntityState
import me.snoty.integration.common.wiring.Node

interface EntityStateService {
	fun scheduleMetricsTask()

	suspend fun updateStates(node: Node, entities: List<IUpdatableEntity<out Any>>)

	fun getStates(node: Node): Flow<EntityState>
}
