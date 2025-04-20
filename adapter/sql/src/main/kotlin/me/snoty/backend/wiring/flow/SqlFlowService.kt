package me.snoty.backend.wiring.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.newSuspendedTransaction
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.utils.toUuid
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.flow.StandaloneWorkflow
import me.snoty.integration.common.wiring.flow.WorkflowSettings
import me.snoty.integration.common.wiring.flow.WorkflowWithNodes
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.koin.core.annotation.Single

@Single
class SqlFlowService(
	private val db: Database,
	private val flowScheduler: FlowScheduler,
	private val nodeService: NodeService,
	private val flowTable: FlowTable,
) : FlowService {
	override suspend fun create(userId: String, name: String, settings: WorkflowSettings): StandaloneWorkflow = db.newSuspendedTransaction {
		val id = flowTable.insertAndGetId {
			it[flowTable.userId] = userId
			it[flowTable.name] = name
			it[flowTable.settings] = settings
		}

		StandaloneWorkflow(_id = id.value.toString(), userId = userId, name = name, settings = settings)
			.also {
				flowScheduler.schedule(it)
			}
	}

	override fun query(userId: String): Flow<StandaloneWorkflow> = db.flowTransaction {
		flowTable.selectStandalone()
			.where { flowTable.userId eq userId }
			.map { it.toStandalone(flowTable) }
	}

	override suspend fun getStandalone(flowId: NodeId): StandaloneWorkflow? = db.newSuspendedTransaction {
		flowTable.selectStandalone()
			.where { flowTable.id eq flowId.toUuid() }
			.firstOrNull()
			?.toStandalone(flowTable)
	}

	override suspend fun getWithNodes(flowId: NodeId): WorkflowWithNodes? = db.newSuspendedTransaction {
		val flow = flowTable.selectStandalone()
			.where { flowTable.id eq flowId.toUuid() }
			.firstOrNull() ?: return@newSuspendedTransaction null

		val nodes = nodeService.getByFlow(flowId).toList()

		flow.let {
			WorkflowWithNodes(
				_id = it[flowTable.id].value.toString(),
				userId = it[flowTable.userId],
				name = it[flowTable.name],
				settings = it[flowTable.settings] ?: WorkflowSettings(),
				nodes = nodes,
			)
		}
	}

	override fun getAll(): Flow<StandaloneWorkflow> = db.flowTransaction {
		flowTable.selectStandalone().map { it.toStandalone(flowTable) }
	}

	override suspend fun rename(flowId: NodeId, name: String) = db.newSuspendedTransaction<Unit> {
		flowTable.update({ flowTable.id eq flowId.toUuid() }) {
			it[flowTable.name] = name
		}
	}

	override suspend fun updateSettings(flowId: NodeId, settings: WorkflowSettings) = db.newSuspendedTransaction {
		val flow = flowTable.updateReturning(flowTable.standaloneColumns, { flowTable.id eq flowId.toUuid() }) {
			it[flowTable.settings] = settings
		}
			.first()
			.toStandalone(flowTable)

		flowScheduler.reschedule(flow)
	}

	override suspend fun delete(flowId: NodeId) = db.newSuspendedTransaction<Unit> {
		flowTable.deleteWhere { id eq flowId.toUuid() }
	}
}
