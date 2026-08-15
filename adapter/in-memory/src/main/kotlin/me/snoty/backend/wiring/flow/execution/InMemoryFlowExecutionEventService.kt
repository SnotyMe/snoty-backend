package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import me.snoty.core.flow.Workflow
import me.snoty.core.user.UserId
import org.koin.core.annotation.Single

@Single
class InMemoryFlowExecutionEventService : FlowExecutionEventService {
	// TODO: configurable buffer size
	val flow = MutableSharedFlow<FlowExecutionEvent>(extraBufferCapacity = 500)

	override suspend fun provideFlowBus(flow: Workflow): Flow<FlowExecutionEvent> =
		this.flow.filter { it.flowId == flow.id }

	override suspend fun provideUserBus(userId: UserId): Flow<FlowExecutionEvent> =
		flow.filter { it.userId == userId }

	override suspend fun offer(event: FlowExecutionEvent) {
		flow.emit(event)
	}
}
