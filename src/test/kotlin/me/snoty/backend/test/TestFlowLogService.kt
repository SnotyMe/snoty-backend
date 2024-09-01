package me.snoty.backend.test

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.flow.logging.FlowLogService
import me.snoty.integration.common.wiring.flow.NodeLogEntry

class TestFlowLogService : FlowLogService {
	private val logs = mutableMapOf<NodeId, MutableList<NodeLogEntry>>()

	override suspend fun record(rootNode: NodeId, entry: NodeLogEntry) {
		logs.getOrPut(rootNode) { mutableListOf() }.add(entry)
	}

	override suspend fun retrieve(rootNode: NodeId): List<NodeLogEntry>
		= logs[rootNode] ?: emptyList()
}
