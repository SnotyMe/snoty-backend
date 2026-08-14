package me.snoty.backend.wiring.flow.export

import me.snoty.backend.wiring.flow.ExportFlow
import me.snoty.integration.common.wiring.flow.Workflow

interface FlowExportService {
	suspend fun export(flow: Workflow, censor: Boolean = true): ExportFlow
}
