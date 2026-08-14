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

	suspend fun getStandalone(userId: UserId, flowId: FlowId): StandaloneWorkflow?
	suspend fun getWithNodes(userId: UserId?, flowId: FlowId): WorkflowWithNodes?

	fun getAll(): Flow<StandaloneWorkflow>

	suspend fun rename(flow: Workflow, name: String)
	suspend fun updateSettings(flow: Workflow, settings: WorkflowSettings)

	suspend fun delete(flow: Workflow)
}
