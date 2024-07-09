package me.snoty.integration.common.wiring.flow

import me.snoty.backend.integration.config.flow.NodeId

data class FlowLogEntry(
	val message: String,
	val node: NodeId
)
