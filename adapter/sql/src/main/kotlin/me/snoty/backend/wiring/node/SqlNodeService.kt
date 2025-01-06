package me.snoty.backend.wiring.node

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.newSuspendedTransaction
import me.snoty.backend.errors.ServiceResult
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.utils.toUuid
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.NodeSettings
import me.snoty.integration.common.wiring.toRelational
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import org.slf4j.event.Level
import java.util.*

@Single
class SqlNodeService(
	private val db: Database,
	private val nodeRegistry: NodeRegistry,
	private val nodeTable: NodeTable,
	private val nodeConnectionTable: NodeConnectionTable,
) : NodeService {
	override suspend fun get(id: NodeId): StandaloneNode? = db.newSuspendedTransaction {
		nodeTable.selectStandalone()
			.where { nodeTable.id eq id.toUuid() }
			.firstOrNull()
			?.toStandalone(nodeTable)
	}

	override fun getByFlow(flowId: NodeId): Flow<FlowNode> = db.flowTransaction {
		val nodes = nodeTable.selectStandalone()
			.where { nodeTable.flowId eq flowId.toUuid() }
			.map { it.toStandalone(nodeTable) }

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

	override fun query(
		userID: UUID?,
		position: NodePosition?
	): Flow<StandaloneNode> = db.flowTransaction {
		val query = nodeTable.selectStandalone()

		if (userID != null) query.andWhere { nodeTable.userId eq userID }
		if (position != null) query.andWhere { positionFilter(nodeTable, nodeRegistry, position) }

		query.map { it.toStandalone(nodeTable) }
	}

	override suspend fun <S : NodeSettings> create(
		userID: UUID,
		flowId: NodeId,
		descriptor: NodeDescriptor,
		settings: S
	): StandaloneNode = db.newSuspendedTransaction {
		val id = nodeTable.insertAndGetId {
			it[nodeTable.flowId] = flowId.toUuid()
			it[nodeTable.userId] = userID
			it[nodeTable.descriptor_namespace] = descriptor.namespace
			it[nodeTable.descriptor_name] = descriptor.name
			it[nodeTable.logLevel] = Level.INFO
			it[nodeTable.settings] = settings
		}

		StandaloneNode(
			_id = id.value.toString(),
			flowId = flowId,
			userId = userID,
			descriptor = descriptor,
			logLevel = Level.INFO,
			settings = settings,
		)
	}

	override suspend fun connect(
		from: NodeId,
		to: NodeId
	): ServiceResult {
		TODO("Not yet implemented")
	}

	override suspend fun disconnect(
		from: NodeId,
		to: NodeId
	): ServiceResult {
		TODO("Not yet implemented")
	}

	override suspend fun updateSettings(
		id: NodeId,
		settings: NodeSettings
	): ServiceResult {
		TODO("Not yet implemented")
	}

	override suspend fun updateLogLevel(id: NodeId, logLevel: Level?): ServiceResult {
		TODO("Not yet implemented")
	}

	override suspend fun delete(id: NodeId): ServiceResult {
		TODO("Not yet implemented")
	}
}
