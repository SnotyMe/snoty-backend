package me.snoty.integration.common.diff

import kotlinx.coroutines.flow.Flow
import me.snoty.integration.common.diff.state.EntityState
import me.snoty.integration.common.wiring.IFlowNode

interface EntityStateService {
	fun scheduleMetricsTask()

	suspend fun updateStates(node: IFlowNode, entities: List<IUpdatableEntity<out Any>>)

	fun getStates(node: IFlowNode): Flow<EntityState>
}
