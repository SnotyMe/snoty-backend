package me.snoty.backend.test

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import me.snoty.integration.common.wiring.flow.EnumeratedFlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.NodeLogEntry
import kotlin.uuid.Uuid

class TestFlowExecutionService : FlowExecutionService {
	data class FlowEntry(val flowId: NodeId, val logs: MutableList<NodeLogEntry>)

	private val logs = mutableMapOf<String, FlowEntry>()

	override suspend fun create(jobId: String, flowId: NodeId, triggeredBy: FlowTriggerReason) {
		logs[jobId] = FlowEntry(flowId = flowId, logs = mutableListOf())
	}

	override suspend fun record(jobId: String, entry: NodeLogEntry) {
		logs[jobId]?.logs?.add(entry)
	}

	override suspend fun retrieve(flowId: NodeId): List<NodeLogEntry>
		= logs.values.filter { entry -> entry.flowId == flowId }.flatMap(FlowEntry::logs)

	override suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus) {
		// NOOP
	}

	override fun query(userId: Uuid): Flow<EnumeratedFlowExecution> = throw NotImplementedError()
	override fun query(flowId: NodeId, startFromNode: String?, limit: Int): Flow<FlowExecution> = throw NotImplementedError()

	override suspend fun deleteAll(flowId: NodeId) = throw NotImplementedError()
}
