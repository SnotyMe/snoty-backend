package me.snoty.integration.common.wiring.flow

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.integration.config.flow.NodeId
import java.util.*

interface FlowService {
	suspend fun create(userId: UUID, name: String): StandaloneWorkflow

	/**
	 * @return a list of [Workflow]s for the given [userId]
	 */
	fun query(userId: UUID): Flow<StandaloneWorkflow>

	suspend fun getStandalone(flowId: NodeId): StandaloneWorkflow?
	suspend fun getWithNodes(flowId: NodeId): WorkflowWithNodes?

	fun getAll(): Flow<StandaloneWorkflow>
}
