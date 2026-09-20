package me.snoty.backend.wiring.node

import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import me.snoty.backend.errors.ServiceResult
import me.snoty.core.flow.FlowId
import me.snoty.core.flow.Workflow
import me.snoty.core.node.*
import me.snoty.core.user.UserId
import org.slf4j.event.Level
import java.util.*

interface NodeService {
	suspend fun get(userId: UserId?, id: NodeId): StandaloneNode?
	fun getByFlow(flowId: FlowId): Flow<FlowNode>

	suspend fun <S : NodeSettings> create(
		userId: UserId,
		flow: Workflow,
		type: NodeType,
		name: String,
		position: NodePosition,
		settings: S,
	): StandaloneNode

	suspend fun connect(from: Node, to: Node): ServiceResult
	suspend fun disconnect(from: Node, to: Node): ServiceResult

	suspend fun patch(node: Node, patchRequest: NodePatch): ServiceResult
	suspend fun updateSettings(node: Node, settings: NodeSettings): ServiceResult

	suspend fun delete(node: Node): ServiceResult
}

object NodeServiceResults {
	class NodeNotFoundError(id: NodeId) : ServiceResult(HttpStatusCode.NotFound, "Node with ID $id not found")
	class NodeConnected(from: Node, to: Node) : ServiceResult(HttpStatusCode.OK, "Connected ${from.id} to ${to.id}")
	class NodeDisconnected(from: Node, to: Node) : ServiceResult(HttpStatusCode.OK, "Disconnected ${from.id} from ${to.id}")
	class NodeUpdated(node: Node) : ServiceResult(HttpStatusCode.OK, "Aspect of node ${node.id} updated")
	class NodeDeleted(node: Node) : ServiceResult(HttpStatusCode.OK, "Node ${node.id} deleted")
}

data class NodePatch(
	val name: String? = null,
	val position: NodePosition? = null,
	val logLevel: Optional<Level?>? = null,
	val settings: NodeSettings? = null,
)
