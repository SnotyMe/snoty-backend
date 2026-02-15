package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.Flow

interface FlowExecutionEventService {
    suspend fun provideFlowBus(flowId: String): Flow<FlowExecutionEvent>
    suspend fun provideUserBus(userId: String): Flow<FlowExecutionEvent>

    /**
     * Offers an event. Will be passed on using the database or in-memory channel.
     */
    suspend fun offer(event: FlowExecutionEvent)
}
