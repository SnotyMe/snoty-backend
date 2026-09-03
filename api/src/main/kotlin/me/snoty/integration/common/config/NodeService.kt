package me.snoty.integration.common.config

import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import me.snoty.backend.errors.ServiceResult
import me.snoty.core.flow.FlowId
import me.snoty.core.flow.Workflow
import me.snoty.core.node.FlowNode
import me.snoty.core.node.Node
import me.snoty.core.node.NodeId
import me.snoty.core.node.StandaloneNode
import me.snoty.core.user.UserId
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeSettings
import org.slf4j.event.Level

interface NodeService {
	suspend fun get(userId: UserId?, id: NodeId): StandaloneNode?
	fun getByFlow(flowId: FlowId): Flow<FlowNode>

	suspend fun <S : NodeSettings> create(
		userId: UserId,
		flow: Workflow,
		descriptor: NodeDescriptor,
		name: String,
		position: NodePosition,
		settings: S,
	): StandaloneNode

	suspend fun connect(from: Node, to: Node): ServiceResult
	suspend fun disconnect(from: Node, to: Node): ServiceResult

	suspend fun updateName(node: Node, name: String): ServiceResult
	suspend fun updatePosition(node: Node, position: NodePosition): ServiceResult
	suspend fun updateSettings(node: Node, settings: NodeSettings): ServiceResult
	suspend fun updateLogLevel(node: Node, logLevel: Level?): ServiceResult

	suspend fun delete(node: Node): ServiceResult
}

object NodeServiceResults {
	class NodeNotFoundError(id: NodeId) : ServiceResult(HttpStatusCode.NotFound, "Node with ID $id not found")
	class NodeConnected(from: Node, to: Node) : ServiceResult(HttpStatusCode.OK, "Connected ${from.id} to ${to.id}")
	class NodeDisconnected(from: Node, to: Node) : ServiceResult(HttpStatusCode.OK, "Disconnected ${from.id} from ${to.id}")
	class NodeUpdated(node: Node) : ServiceResult(HttpStatusCode.OK, "Aspect of node ${node.id} updated")
	class NodeDeleted(node: Node) : ServiceResult(HttpStatusCode.OK, "Node ${node.id} deleted")
}
