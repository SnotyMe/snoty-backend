package me.snoty.backend.wiring.node

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.suspendTransaction
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.utils.hackyEncodeToString
import me.snoty.core.FlowId
import me.snoty.core.NodeId
import me.snoty.core.UserId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.config.NodeServiceResults
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.toRelational
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
	override suspend fun get(id: NodeId): StandaloneNode? = db.suspendTransaction {
		nodeTable.selectAll()
			.where { nodeTable.id eq id }
			.firstOrNull()
			?.toStandalone(nodeTable, json, nodeRegistry)
	}

	override fun getByFlow(flowId: FlowId): Flow<FlowNode> = db.flowTransaction {
		val nodes = nodeTable.selectAll()
			.where { nodeTable.flowId eq flowId }
			.map { it.toStandalone(nodeTable, json, nodeRegistry) }

		val connections = nodeConnectionTable.selectAll()
			.where { nodeConnectionTable.from inList nodes.map { it._id } }
			.map { it[nodeConnectionTable.from].value to it[nodeConnectionTable.to].value }
			.groupBy({ it.first }, { it.second })

		nodes.map { node ->
			node.toRelational(
				next = connections[node._id]
			)
		}
	}

	override suspend fun <S : NodeSettings> create(
		userId: UserId,
		flowId: FlowId,
		descriptor: NodeDescriptor,
		position: NodePosition,
		settings: S
	): StandaloneNode {
		val id = db.suspendTransaction {
			nodeTable.insertAndGetId {
				it[nodeTable.flowId] = flowId
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
			_id = id.value,
			flowId = flowId,
			userId = userId,
			descriptor = descriptor,
			logLevel = null,
			position = position,
			settings = settings,
		)
	}

	override suspend fun connect(
		from: NodeId,
		to: NodeId
	): ServiceResult = db.suspendTransaction {
		val insertCount = nodeConnectionTable.insert {
			it[nodeConnectionTable.from] = from
			it[nodeConnectionTable.to] = to
		}.insertedCount

		when (insertCount) {
			0 -> NodeServiceResults.NodeNotFoundError(from)
			else -> NodeServiceResults.NodeConnected(from, to)
		}
	}

	override suspend fun disconnect(
		from: NodeId,
		to: NodeId
	): ServiceResult = db.suspendTransaction {
		val deleteCount = nodeConnectionTable.deleteWhere {
			(nodeConnectionTable.from eq from) and (nodeConnectionTable.to eq to)
		}

		when (deleteCount) {
			0 -> NodeServiceResults.NodeNotFoundError(from)
			else -> NodeServiceResults.NodeDisconnected(from, to)
		}
	}

	override suspend fun updatePosition(id: NodeId, position: NodePosition) = updateNode(id) {
		it[nodeTable.positionX] = position.x
		it[nodeTable.positionY] = position.y
		it[nodeTable.width] = position.width
		it[nodeTable.height] = position.height
	}

	override suspend fun updateSettings(id: NodeId, settings: NodeSettings) = updateNode(id) {
		it[nodeTable.settings] = json.hackyEncodeToString(settings)
	}

	override suspend fun updateLogLevel(id: NodeId, logLevel: Level?) = updateNode(id) {
		it[nodeTable.logLevel] = logLevel
	}

	private suspend fun updateNode(id: NodeId, update: NodeTable.(UpdateStatement) -> Unit): ServiceResult {
		val changeCount = db.suspendTransaction {
			nodeTable.update(where = { nodeTable.id eq id }, body = update)
		}
		return when (changeCount) {
			0 -> NodeServiceResults.NodeNotFoundError(id)
			else -> NodeServiceResults.NodeUpdated(id)
		}
	}

	override suspend fun delete(id: NodeId): ServiceResult = db.suspendTransaction {
		when (nodeTable.deleteWhere { nodeTable.id eq id }) {
			0 -> NodeServiceResults.NodeNotFoundError(id)
			else -> NodeServiceResults.NodeDeleted(id)
		}
	}
}
