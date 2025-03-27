package me.snoty.backend.wiring.node

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.newSuspendedTransaction
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.utils.hackyEncodeToString
import me.snoty.backend.utils.toUuid
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.config.NodeServiceResults
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.toRelational
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.koin.core.annotation.Single
import org.slf4j.event.Level
import java.util.*

@Single
class SqlNodeService(
	private val db: Database,
	private val json: Json,
	private val nodeRegistry: NodeRegistry,
	private val nodeTable: NodeTable,
	private val nodeConnectionTable: NodeConnectionTable,
) : NodeService {
	override suspend fun get(id: NodeId): StandaloneNode? = db.newSuspendedTransaction {
		nodeTable.selectStandalone()
			.where { nodeTable.id eq id.toUuid() }
			.firstOrNull()
			?.toStandalone(nodeTable, json, nodeRegistry)
	}

	override fun getByFlow(flowId: NodeId): Flow<FlowNode> = db.flowTransaction {
		val nodes = nodeTable.selectStandalone()
			.where { nodeTable.flowId eq flowId.toUuid() }
			.map { it.toStandalone(nodeTable, json, nodeRegistry) }

		val connections = nodeConnectionTable.selectAll()
			.where { nodeConnectionTable.from inList nodes.map { it._id.toUuid() } }
			.map { it[nodeConnectionTable.from].value to it[nodeConnectionTable.to].value }
			.groupBy({ it.first }, { it.second })

		nodes.map { node ->
			node.toRelational(
				next = connections[node._id.toUuid()]
					?.map { it.toString() }
			)
		}
	}

	override suspend fun <S : NodeSettings> create(
		userID: UUID,
		flowId: NodeId,
		descriptor: NodeDescriptor,
		settings: S
	): StandaloneNode {
		val id = db.newSuspendedTransaction {
			nodeTable.insertAndGetId {
				it[nodeTable.flowId] = flowId.toUuid()
				it[nodeTable.userId] = userID
				it[nodeTable.descriptor_namespace] = descriptor.namespace
				it[nodeTable.descriptor_name] = descriptor.name
				it[nodeTable.settings] = json.hackyEncodeToString(settings)
			}
		}

		return StandaloneNode(
			_id = id.value.toString(),
			flowId = flowId,
			userId = userID,
			descriptor = descriptor,
			logLevel = null,
			settings = settings,
		)
	}

	override suspend fun connect(
		from: NodeId,
		to: NodeId
	): ServiceResult = db.newSuspendedTransaction {
		val insertCount = nodeConnectionTable.insert {
			it[nodeConnectionTable.from] = from.toUuid()
			it[nodeConnectionTable.to] = to.toUuid()
		}.insertedCount

		when (insertCount) {
			0 -> NodeServiceResults.NodeNotFoundError(from)
			else -> NodeServiceResults.NodeConnected(from, to)
		}
	}

	override suspend fun disconnect(
		from: NodeId,
		to: NodeId
	): ServiceResult = db.newSuspendedTransaction {
		val deleteCount = nodeConnectionTable.deleteWhere {
			(nodeConnectionTable.from eq from.toUuid()) and (nodeConnectionTable.to eq to.toUuid())
		}

		when (deleteCount) {
			0 -> NodeServiceResults.NodeNotFoundError(from)
			else -> NodeServiceResults.NodeDisconnected(from, to)
		}
	}

	override suspend fun updateSettings(
		id: NodeId,
		settings: NodeSettings
	): ServiceResult = db.newSuspendedTransaction {
		val changeCount = nodeTable.update({ nodeTable.id eq id.toUuid() }) {
			it[nodeTable.settings] = json.hackyEncodeToString(settings)
		}
		when (changeCount) {
			0 -> NodeServiceResults.NodeNotFoundError(id)
			else -> NodeServiceResults.NodeSettingsUpdated(id)
		}
	}

	override suspend fun updateLogLevel(id: NodeId, logLevel: Level?): ServiceResult = db.newSuspendedTransaction {
		val changeCount = nodeTable.update({ nodeTable.id eq id.toUuid() }) {
			it[nodeTable.logLevel] = logLevel
		}
		when (changeCount) {
			0 -> NodeServiceResults.NodeNotFoundError(id)
			else -> NodeServiceResults.NodeLogLevelUpdated(id)
		}
	}

	override suspend fun delete(id: NodeId): ServiceResult = db.newSuspendedTransaction {
		when (nodeTable.deleteWhere { nodeTable.id eq id.toUuid() }) {
			0 -> NodeServiceResults.NodeNotFoundError(id)
			else -> NodeServiceResults.NodeDeleted(id)
		}
	}
}
