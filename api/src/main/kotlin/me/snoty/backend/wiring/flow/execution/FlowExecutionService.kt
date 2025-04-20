package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.integration.common.wiring.flow.EnumeratedFlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.NodeLogEntry

interface FlowExecutionService {
	suspend fun create(jobId: String, flowId: NodeId, triggeredBy: FlowTriggerReason)
	suspend fun record(jobId: String, entry: NodeLogEntry)
	suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus)

	suspend fun retrieve(flowId: NodeId): List<NodeLogEntry>
	fun query(userId: String): Flow<EnumeratedFlowExecution>
	fun query(flowId: NodeId, startFrom: String?, limit: Int = 15): Flow<FlowExecution>

	suspend fun deleteAll(flowId: NodeId)
}
