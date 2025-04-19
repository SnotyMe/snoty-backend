package me.snoty.backend.wiring.flow.import

import me.snoty.backend.database.sql.newSuspendedTransaction
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.utils.toUuid
import me.snoty.backend.wiring.node.NodeConnectionTable
import me.snoty.backend.wiring.node.NodeSettingsDeserializationService
import me.snoty.backend.wiring.node.SqlNodeService
import me.snoty.integration.common.wiring.flow.FlowService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.batchInsert
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class SqlFlowImportService(
	private val db: Database,
	private val flowService: FlowService,
	private val nodeConnectionTable: NodeConnectionTable,
	private val nodeService: SqlNodeService,
	private val nodeSettingsDeserializationService: NodeSettingsDeserializationService,
) : FlowImportService {
	override suspend fun import(userId: Uuid, flow: ImportFlow): NodeId = db.newSuspendedTransaction {
		val createdFlow = flowService.create(userId, flow.name, flow.settings)

		val createdNodes = flow.nodes.associate {
			it.id to nodeService.create(
				userID = userId,
				flowId = createdFlow._id,
				descriptor = it.descriptor,
				settings = nodeSettingsDeserializationService.deserializeOrInvalid(it.descriptor, it.settings)
			)._id
		}
		val connections = flow.nodes
			.flatMap { node ->
				node.next.map { nextNode -> node.id to nextNode }
			}
		nodeConnectionTable.batchInsert(connections) { (from, to) ->
			this[nodeConnectionTable.from] = createdNodes[from]!!.toUuid()
			this[nodeConnectionTable.to] = createdNodes[to]!!.toUuid()
		}

		return@newSuspendedTransaction createdFlow._id
	}
}
