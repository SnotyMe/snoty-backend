package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.core.FlowId
import me.snoty.core.UserId
import me.snoty.integration.common.wiring.flow.EnumeratedFlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.NodeLogEntry

interface FlowExecutionService {
	suspend fun create(jobId: String, flowId: FlowId, triggeredBy: FlowTriggerReason)
	suspend fun record(jobId: String, entry: NodeLogEntry)
	suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus)

	fun query(userId: UserId): Flow<EnumeratedFlowExecution>
	fun query(flowId: FlowId, startFrom: String?, limit: Int = 15): Flow<FlowExecution>

	suspend fun deleteAll(flowId: FlowId)
}
