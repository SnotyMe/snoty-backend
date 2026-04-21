package me.snoty.backend.test

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.wiring.flow.execution.FlowExecutionService
import me.snoty.core.FlowId
import me.snoty.core.UserId
import me.snoty.integration.common.wiring.flow.*

class TestFlowExecutionService : FlowExecutionService {
	data class FlowEntry(val flowId: FlowId, val logs: MutableList<NodeLogEntryDto>)

	private val logs = mutableMapOf<String, FlowEntry>()

	override suspend fun create(jobId: String, flowId: FlowId, triggeredBy: FlowTriggerReason) {
		logs[jobId] = FlowEntry(flowId = flowId, logs = mutableListOf())
	}

	override suspend fun record(jobId: String, entry: NodeLogEntry): NodeLogEntryDto {
		val nodeLogEntryDto = NodeLogEntryDto(
			id = randomString(),
			timestamp = entry.timestamp,
			level = entry.level,
			message = entry.message,
			node = entry.node
		)
		logs[jobId]?.logs?.add(nodeLogEntryDto)
		return nodeLogEntryDto
	}

	override suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus) {
		// NOOP
	}

	override fun query(userId: UserId): Flow<EnumeratedFlowExecution> = throw NotImplementedError()
	override fun query(flowId: FlowId, startFrom: String?, limit: Int): Flow<FlowExecution> = throw NotImplementedError()

	override suspend fun deleteAll(flowId: FlowId) = throw NotImplementedError()
}
