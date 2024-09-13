package me.snoty.backend.test

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.logging.FlowLogService
import me.snoty.integration.common.wiring.flow.FlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.NodeLogEntry
import java.util.*

class TestFlowLogService : FlowLogService {
	private val logs = mutableMapOf<NodeId, MutableList<NodeLogEntry>>()

	override suspend fun record(jobId: String, flowId: NodeId, entry: NodeLogEntry) {
		logs.getOrPut(flowId) { mutableListOf() }.add(entry)
	}

	override suspend fun retrieve(flowId: NodeId): List<NodeLogEntry>
		= logs[flowId] ?: emptyList()

	override suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus) {
		// NOOP
	}

	override fun query(userId: UUID): Flow<FlowExecution> = throw NotImplementedError()
}
