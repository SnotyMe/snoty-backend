package me.snoty.backend.wiring.flow.export

import me.snoty.backend.wiring.flow.ExportFlow
import me.snoty.core.FlowId

interface FlowExportService {
	suspend fun export(flowId: FlowId, censor: Boolean = true): ExportFlow
}
