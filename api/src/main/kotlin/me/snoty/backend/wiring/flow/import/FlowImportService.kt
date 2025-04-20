package me.snoty.backend.wiring.flow.import

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.wiring.flow.ImportFlow

interface FlowImportService {
	suspend fun import(userId: String, flow: ImportFlow): NodeId
}
