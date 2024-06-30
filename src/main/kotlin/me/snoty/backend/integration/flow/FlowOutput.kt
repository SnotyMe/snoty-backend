package me.snoty.backend.integration.flow

import me.snoty.backend.integration.config.flow.NodeId

data class FlowOutput(
	val message: String,
	val node: NodeId
)
