package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.core.flow.Workflow
import me.snoty.core.user.UserId
import me.snoty.integration.common.wiring.flow.*

interface FlowExecutionService {
	suspend fun create(jobId: String, flow: Workflow, triggeredBy: FlowTriggerReason)
	suspend fun record(jobId: String, entry: NodeLogEntry): NodeLogEntryDto
	suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus)

	fun query(userId: UserId): Flow<EnumeratedFlowExecution>
	fun query(flow: Workflow, startFrom: String?, limit: Int = 15): Flow<FlowExecution>

	suspend fun deleteAll(flow: Workflow)
}
