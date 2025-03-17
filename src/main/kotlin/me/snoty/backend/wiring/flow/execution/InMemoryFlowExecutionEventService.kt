package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Single

@Single
class InMemoryFlowExecutionEventService : FlowExecutionEventService {
	// TODO: configurable buffer size
	val flow = MutableSharedFlow<FlowExecutionEvent>(extraBufferCapacity = 500)
	
	override fun provideBus() = flow.asSharedFlow()

	override suspend fun offer(event: FlowExecutionEvent) {
		flow.emit(event)
	}
}