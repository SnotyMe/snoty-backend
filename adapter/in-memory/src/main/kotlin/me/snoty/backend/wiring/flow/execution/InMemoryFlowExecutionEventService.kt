package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import org.koin.core.annotation.Single

@Single
class InMemoryFlowExecutionEventService : FlowExecutionEventService {
	// TODO: configurable buffer size
	val flow = MutableSharedFlow<FlowExecutionEvent>(extraBufferCapacity = 500)

	override suspend fun provideFlowBus(flowId: String): Flow<FlowExecutionEvent> =
		flow.filter { it.flowId == flowId }

	override suspend fun provideUserBus(userId: String): Flow<FlowExecutionEvent> =
		flow.filter { it.userId.toString() == userId }

	override suspend fun offer(event: FlowExecutionEvent) {
		flow.emit(event)
	}
}
