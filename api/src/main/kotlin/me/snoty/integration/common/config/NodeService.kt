package me.snoty.integration.common.config

import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import org.slf4j.event.Level
import java.util.*

interface NodeService {
	suspend fun get(id: NodeId): StandaloneNode?
	fun getByFlow(flowId: NodeId): Flow<FlowNode>

	suspend fun <S : NodeSettings> create(
		userID: UUID,
		flowId: NodeId,
		descriptor: NodeDescriptor,
		settings: S,
	): StandaloneNode

	suspend fun connect(from: NodeId, to: NodeId): ServiceResult
	suspend fun disconnect(from: NodeId, to: NodeId): ServiceResult

	suspend fun updateSettings(id: NodeId, settings: NodeSettings): ServiceResult
	suspend fun updateLogLevel(id: NodeId, logLevel: Level?): ServiceResult

	suspend fun delete(id: NodeId): ServiceResult
}

object NodeServiceResults {
	class NodeNotFoundError(id: NodeId) : ServiceResult(HttpStatusCode.NotFound, "Node with ID $id not found")
	class NodeConnected(from: NodeId, to: NodeId) : ServiceResult(HttpStatusCode.OK, "Connected $from to $to")
	class NodeDisconnected(from: NodeId, to: NodeId) : ServiceResult(HttpStatusCode.OK, "Disconnected $from from $to")
	class NodeSettingsUpdated(id: NodeId) : ServiceResult(HttpStatusCode.OK, "Settings for node $id updated")
	class NodeLogLevelUpdated(id: NodeId) : ServiceResult(HttpStatusCode.OK, "Log level for node $id updated")
	class NodeDeleted(id: NodeId) : ServiceResult(HttpStatusCode.OK, "Node $id deleted")
}
