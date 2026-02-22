package me.snoty.integration.common.wiring.flow

import kotlinx.coroutines.flow.Flow
import me.snoty.core.FlowId
import me.snoty.core.UserId

interface FlowService {
	suspend fun create(userId: UserId, name: String, settings: WorkflowSettings): StandaloneWorkflow

	/**
	 * @return a list of [Workflow]s for the given [userId]
	 */
	fun query(userId: UserId): Flow<StandaloneWorkflow>

	suspend fun getStandalone(flowId: FlowId): StandaloneWorkflow?
	suspend fun getWithNodes(flowId: FlowId): WorkflowWithNodes?

	fun getAll(): Flow<StandaloneWorkflow>

	suspend fun rename(flowId: FlowId, name: String)
	suspend fun updateSettings(flowId: FlowId, settings: WorkflowSettings)

	suspend fun delete(flowId: FlowId)
}
