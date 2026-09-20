package me.snoty.backend.wiring.flow.import

import me.snoty.backend.database.sql.suspendTransaction
import me.snoty.backend.wiring.flow.FlowService
import me.snoty.backend.wiring.flow.ImportFlow
import me.snoty.backend.wiring.node.NodeConnectionTable
import me.snoty.backend.wiring.node.NodeSettingsDeserializationService
import me.snoty.backend.wiring.node.SqlNodeService
import me.snoty.core.flow.FlowId
import me.snoty.core.user.UserId
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.koin.core.annotation.Single

@Single
class SqlFlowImportService(
	private val db: Database,
	private val flowService: FlowService,
	private val nodeConnectionTable: NodeConnectionTable,
	private val nodeService: SqlNodeService,
	private val nodeSettingsDeserializationService: NodeSettingsDeserializationService,
) : FlowImportService {
	override suspend fun import(userId: UserId, flow: ImportFlow): FlowId = db.suspendTransaction {
		val createdFlow = flowService.create(userId, flow.name, flow.settings)

		val createdNodes = flow.nodes.associate {
			it.id to nodeService.create(
				userId = userId,
				flow = createdFlow,
				type = it.type,
				name = it.name,
				position = it.position,
				settings = nodeSettingsDeserializationService.deserializeOrInvalid(it.type, it.settings)
			).id
		}
		val connections = flow.nodes
			.flatMap { node ->
				node.next.map { nextNode -> node.id to nextNode }
			}
		nodeConnectionTable.batchInsert(connections, useMultiRowValues = true) { (from, to) ->
			this[nodeConnectionTable.from] = createdNodes[from]!!
			this[nodeConnectionTable.to] = createdNodes[to]!!
		}

		return@suspendTransaction createdFlow.id
	}
}
