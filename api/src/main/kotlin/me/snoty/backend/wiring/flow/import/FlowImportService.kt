package me.snoty.backend.wiring.flow.import

import me.snoty.backend.integration.config.flow.NodeId
import java.util.UUID

interface FlowImportService {
	suspend fun import(userId: UUID, flow: ImportFlow): NodeId
}
