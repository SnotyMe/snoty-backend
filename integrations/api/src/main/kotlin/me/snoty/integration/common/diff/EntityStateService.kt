package me.snoty.integration.common.diff

import kotlinx.coroutines.flow.Flow
import me.snoty.integration.common.InstanceId
import me.snoty.integration.common.diff.state.EntityState
import java.util.UUID

interface EntityStateService {
	fun scheduleMetricsTask()

	suspend fun updateStates(userID: UUID, instanceId: InstanceId, entities: List<IUpdatableEntity<out Any>>)

	fun getEntities(userID: UUID, instanceId: InstanceId, type: String): Flow<EntityState>
}
