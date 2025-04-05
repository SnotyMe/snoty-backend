package me.snoty.backend.wiring.flow.import

import me.snoty.backend.integration.config.flow.NodeId
import kotlin.uuid.Uuid

interface FlowImportService {
	suspend fun import(userId: Uuid, flow: ImportFlow): NodeId
}
