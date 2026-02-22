package me.snoty.backend.test

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import me.snoty.core.FlowId
import me.snoty.core.UserId
import me.snoty.integration.common.wiring.flow.EnumeratedFlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.NodeLogEntry

class TestFlowExecutionService : FlowExecutionService {
	data class FlowEntry(val flowId: FlowId, val logs: MutableList<NodeLogEntry>)

	private val logs = mutableMapOf<String, FlowEntry>()

	override suspend fun create(jobId: String, flowId: FlowId, triggeredBy: FlowTriggerReason) {
		logs[jobId] = FlowEntry(flowId = flowId, logs = mutableListOf())
	}

	override suspend fun record(jobId: String, entry: NodeLogEntry) {
		logs[jobId]?.logs?.add(entry)
	}

	override suspend fun retrieve(flowId: FlowId): List<NodeLogEntry>
		= logs.values.filter { entry -> entry.flowId == flowId }.flatMap(FlowEntry::logs)

	override suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus) {
		// NOOP
	}

	override fun query(userId: UserId): Flow<EnumeratedFlowExecution> = throw NotImplementedError()
	override fun query(flowId: FlowId, startFrom: String?, limit: Int): Flow<FlowExecution> = throw NotImplementedError()

	override suspend fun deleteAll(flowId: FlowId) = throw NotImplementedError()
}
