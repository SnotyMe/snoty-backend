package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.Flow
import me.snoty.core.FlowId
import me.snoty.core.UserId

interface FlowExecutionEventService {
    suspend fun provideFlowBus(flowId: FlowId): Flow<FlowExecutionEvent>
    suspend fun provideUserBus(userId: UserId): Flow<FlowExecutionEvent>

    /**
     * Offers an event. Will be passed on using the database or in-memory channel.
     */
    suspend fun offer(event: FlowExecutionEvent)
}
