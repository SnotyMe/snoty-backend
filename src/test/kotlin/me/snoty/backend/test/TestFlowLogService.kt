package me.snoty.backend.test

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.logging.FlowLogService
import me.snoty.integration.common.wiring.flow.NodeLogEntry

class TestFlowLogService : FlowLogService {
	private val logs = mutableMapOf<NodeId, MutableList<NodeLogEntry>>()

	override suspend fun record(jobId: String, flowId: NodeId, entry: NodeLogEntry) {
		logs.getOrPut(flowId) { mutableListOf() }.add(entry)
	}

	override suspend fun retrieve(flowId: NodeId): List<NodeLogEntry>
		= logs[flowId] ?: emptyList()
}
