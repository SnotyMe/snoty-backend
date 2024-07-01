package me.snoty.integration.common.config

import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeSettings
import java.util.*

interface NodeService {
	fun getByUser(userID: UUID, position: NodePosition?): Flow<IFlowNode>

	fun getAll(integrationType: String): Flow<IFlowNode>

	suspend fun get(id: NodeId): IFlowNode?

	/**
	 * @return ID of the newly created entry
	 */
	suspend fun <S : NodeSettings> create(userID: UUID, descriptor: NodeDescriptor, settings: S): NodeId

	suspend fun connect(from: NodeId, to: NodeId): ServiceResult
}

object NodeServiceResults {
	class NodeNotFoundError(val id: NodeId) : ServiceResult(HttpStatusCode.NotFound, "Node with ID $id not found")
	class NodeConnected(from: NodeId, to: NodeId) : ServiceResult(HttpStatusCode.OK, "Connected $from to $to")
}
