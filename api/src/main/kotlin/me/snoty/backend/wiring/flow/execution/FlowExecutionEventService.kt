package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.Flow
import me.snoty.core.flow.Workflow
import me.snoty.core.user.UserId

interface FlowExecutionEventService {
    suspend fun provideFlowBus(flow: Workflow): Flow<FlowExecutionEvent>
    suspend fun provideUserBus(userId: UserId): Flow<FlowExecutionEvent>

    /**
     * Offers an event. Will be passed on using the database or in-memory channel.
     */
    suspend fun offer(event: FlowExecutionEvent)
}
