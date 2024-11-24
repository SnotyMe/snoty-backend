package me.snoty.backend.wiring.flow.export

import me.snoty.backend.integration.config.flow.NodeId

interface FlowExportService {
	suspend fun export(flowId: NodeId, censor: Boolean = true): ExportedFlow
}
