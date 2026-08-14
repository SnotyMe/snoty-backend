package me.snoty.backend.wiring.node

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.suspendTransaction
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.utils.hackyEncodeToString
import me.snoty.core.FlowId
import me.snoty.core.Node
import me.snoty.core.NodeId
import me.snoty.core.UserId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.config.NodeServiceResults
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.flow.Workflow
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.toRelational
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.jdbc.*
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@Single
class SqlNodeService(
	private val db: Database,
	private val json: Json,
	private val nodeRegistry: NodeRegistry,
	private val nodeTable: NodeTable,
	private val nodeConnectionTable: NodeConnectionTable,
) : NodeService {
	override suspend fun get(userId: UserId?, id: NodeId): StandaloneNode? = db.suspendTransaction {
		nodeTable.selectAll()
			.where { (if (userId != null) nodeTable.userId eq userId else Op.TRUE) and (nodeTable.id eq id) }
			.firstOrNull()
			?.toStandalone(nodeTable, json, nodeRegistry)
	}

	override fun getByFlow(flowId: FlowId): Flow<FlowNode> = db.flowTransaction {
		val nodes = nodeTable.selectAll()
			.where { nodeTable.flowId eq flowId }
			.map { it.toStandalone(nodeTable, json, nodeRegistry) }

		val connections = nodeConnectionTable.selectAll()
			.where { nodeConnectionTable.from inList nodes.map { it.id } }
			.map { it[nodeConnectionTable.from].value to it[nodeConnectionTable.to].value }
			.groupBy({ it.first }, { it.second })

		nodes.map { node ->
			node.toRelational(
				next = connections[node.id]
			)
		}
	}

	override suspend fun <S : NodeSettings> create(
		userId: UserId,
		flow: Workflow,
		descriptor: NodeDescriptor,
		position: NodePosition,
		settings: S
	): StandaloneNode {
		val id = db.suspendTransaction {
			nodeTable.insertAndGetId {
				it[nodeTable.flowId] = flow.id
				it[nodeTable.userId] = userId
				it[nodeTable.descriptor_namespace] = descriptor.namespace
				it[nodeTable.descriptor_name] = descriptor.name
				it[nodeTable.positionX] = position.x
				it[nodeTable.positionY] = position.y
				it[nodeTable.width] = position.width
				it[nodeTable.height] = position.height
				it[nodeTable.settings] = json.hackyEncodeToString(settings)
			}
		}

		return StandaloneNode(
			id = id.value,
			flowId = flow.id,
			userId = userId,
			descriptor = descriptor,
			logLevel = null,
			position = position,
			settings = settings,
		)
	}

	override suspend fun connect(from: Node, to: Node): ServiceResult = db.suspendTransaction {
		val insertCount = nodeConnectionTable.insert {
			it[nodeConnectionTable.from] = from.id
			it[nodeConnectionTable.to] = to.id
		}.insertedCount

		when (insertCount) {
			0 -> NodeServiceResults.NodeNotFoundError(from.id)
			else -> NodeServiceResults.NodeConnected(from, to)
		}
	}

	override suspend fun disconnect(from: Node, to: Node): ServiceResult = db.suspendTransaction {
		val deleteCount = nodeConnectionTable.deleteWhere {
			(nodeConnectionTable.from eq from.id) and (nodeConnectionTable.to eq to.id)
		}

		when (deleteCount) {
			0 -> NodeServiceResults.NodeNotFoundError(from.id)
			else -> NodeServiceResults.NodeDisconnected(from, to)
		}
	}

	override suspend fun updatePosition(node: Node, position: NodePosition) = updateNode(node) {
		it[nodeTable.positionX] = position.x
		it[nodeTable.positionY] = position.y
		it[nodeTable.width] = position.width
		it[nodeTable.height] = position.height
	}

	override suspend fun updateSettings(node: Node, settings: NodeSettings) = updateNode(node) {
		it[nodeTable.settings] = json.hackyEncodeToString(settings)
	}

	override suspend fun updateLogLevel(node: Node, logLevel: Level?) = updateNode(node) {
		it[nodeTable.logLevel] = logLevel
	}

	private suspend fun updateNode(node: Node, update: NodeTable.(UpdateStatement) -> Unit): ServiceResult {
		val changeCount = db.suspendTransaction {
			nodeTable.update(where = { nodeTable.id eq node.id }, body = update)
		}
		return when (changeCount) {
			0 -> NodeServiceResults.NodeNotFoundError(node.id)
			else -> NodeServiceResults.NodeUpdated(node)
		}
	}

	override suspend fun delete(node: Node): ServiceResult = db.suspendTransaction {
		when (nodeTable.deleteWhere { nodeTable.id eq id }) {
			0 -> NodeServiceResults.NodeNotFoundError(node.id)
			else -> NodeServiceResults.NodeDeleted(node)
		}
	}
}
