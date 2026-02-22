package me.snoty.backend.wiring.flow.import

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.wiring.flow.ImportFlow
import me.snoty.core.UserId

interface FlowImportService {
	suspend fun import(userId: UserId, flow: ImportFlow): NodeId
}
