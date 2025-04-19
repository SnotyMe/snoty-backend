package me.snoty.backend.wiring.flow.export

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.wiring.flow.ExportFlow

interface FlowExportService {
	suspend fun export(flowId: NodeId, censor: Boolean = true): ExportFlow
}
