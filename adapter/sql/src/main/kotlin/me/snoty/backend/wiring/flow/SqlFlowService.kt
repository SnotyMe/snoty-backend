package me.snoty.backend.wiring.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.suspendTransaction
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.core.flow.*
import me.snoty.core.user.UserId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.flow.FlowService
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.koin.core.annotation.Single

@Single
class SqlFlowService(
	private val db: Database,
	private val flowScheduler: FlowScheduler,
	private val nodeService: NodeService,
	private val flowTable: FlowTable,
) : FlowService {
	override suspend fun create(userId: UserId, name: String, settings: WorkflowSettings): StandaloneWorkflow = db.suspendTransaction {
		val row = flowTable.insertReturning(flowTable.standaloneColumns) {
			it[flowTable.userId] = userId
			it[flowTable.name] = name
			it[flowTable.settings] = settings
		}.first()

		row.toStandalone(flowTable)
			.also {
				flowScheduler.schedule(it)
			}
	}

	override fun query(userId: UserId): Flow<StandaloneWorkflow> = db.flowTransaction {
		flowTable.selectStandalone()
			.where { flowTable.userId eq userId }
			.map { it.toStandalone(flowTable) }
	}

	override suspend fun getStandalone(userId: UserId, flowId: FlowId): StandaloneWorkflow? = db.suspendTransaction {
		flowTable.selectStandalone()
			.where { (flowTable.userId eq userId) and (flowTable.id eq flowId) }
			.firstOrNull()
			?.toStandalone(flowTable)
	}

	override suspend fun getWithNodes(userId: UserId?, flowId: FlowId): WorkflowWithNodes? = db.suspendTransaction {
		val flow = flowTable.selectStandalone()
			.where { (if (userId != null) flowTable.userId eq userId else Op.TRUE) and (flowTable.id eq flowId) }
			.firstOrNull() ?: return@suspendTransaction null

		val nodes = nodeService.getByFlow(flowId).toList()

		flow.let {
			WorkflowWithNodes(
				id = it[flowTable.id].value,
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

	override suspend fun rename(flow: Workflow, name: String) = db.suspendTransaction<Unit> {
		flowTable.update({ flowTable.id eq flow.id }) {
			it[flowTable.name] = name
		}
	}

	override suspend fun updateSettings(flow: Workflow, settings: WorkflowSettings) = db.suspendTransaction {
		val flow = flowTable.updateReturning(flowTable.standaloneColumns, { flowTable.id eq flow.id }) {
			it[flowTable.settings] = settings
		}
			.first()
			.toStandalone(flowTable)

		flowScheduler.reschedule(flow)
	}

	override suspend fun delete(flow: Workflow) = db.suspendTransaction<Unit> {
		flowTable.deleteWhere { id eq flow.id }
	}
}
