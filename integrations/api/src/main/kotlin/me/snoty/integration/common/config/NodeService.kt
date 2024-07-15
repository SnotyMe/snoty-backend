package me.snoty.integration.common.config

import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeSettings
import java.util.*

interface NodeService {
	suspend fun get(id: NodeId): StandaloneNode?

	fun getAll(integrationType: String): Flow<StandaloneNode>

	fun getByUser(userID: UUID, position: NodePosition?): Flow<StandaloneNode>

	/**
	 * @return ID of the newly created entry
	 */
	suspend fun <S : NodeSettings> create(userID: UUID, descriptor: NodeDescriptor, settings: S): NodeId

	suspend fun connect(from: NodeId, to: NodeId): ServiceResult
	suspend fun disconnect(from: NodeId, to: NodeId): ServiceResult

	suspend fun updateSettings(id: NodeId, settings: NodeSettings): ServiceResult
}

object NodeServiceResults {
	class NodeNotFoundError(id: NodeId) : ServiceResult(HttpStatusCode.NotFound, "Node with ID $id not found")
	class NodeConnected(from: NodeId, to: NodeId) : ServiceResult(HttpStatusCode.OK, "Connected $from to $to")
	class NodeDisconnected(from: NodeId, to: NodeId) : ServiceResult(HttpStatusCode.OK, "Disconnected $from from $to")
	class NodeSettingsUpdated(id: NodeId) : ServiceResult(HttpStatusCode.OK, "Settings for node $id updated")
}
