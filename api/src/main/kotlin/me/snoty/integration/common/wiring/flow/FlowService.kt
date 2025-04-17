package me.snoty.integration.common.wiring.flow

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.integration.config.flow.NodeId
import kotlin.uuid.Uuid

interface FlowService {
	suspend fun create(userId: Uuid, name: String, settings: WorkflowSettings): StandaloneWorkflow

	/**
	 * @return a list of [Workflow]s for the given [userId]
	 */
	fun query(userId: Uuid): Flow<StandaloneWorkflow>

	suspend fun getStandalone(flowId: NodeId): StandaloneWorkflow?
	suspend fun getWithNodes(flowId: NodeId): WorkflowWithNodes?

	fun getAll(): Flow<StandaloneWorkflow>

	suspend fun rename(flowId: NodeId, name: String)
	suspend fun updateSettings(flowId: NodeId, settings: WorkflowSettings)

	suspend fun delete(flowId: NodeId)
}
