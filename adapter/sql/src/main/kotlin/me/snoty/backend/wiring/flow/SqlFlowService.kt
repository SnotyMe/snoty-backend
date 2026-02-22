package me.snoty.backend.wiring.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.suspendTransaction
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.core.FlowId
import me.snoty.core.UserId
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.flow.StandaloneWorkflow
import me.snoty.integration.common.wiring.flow.WorkflowSettings
import me.snoty.integration.common.wiring.flow.WorkflowWithNodes
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
		val id = flowTable.insertAndGetId {
			it[flowTable.userId] = userId
			it[flowTable.name] = name
			it[flowTable.settings] = settings
		}

		StandaloneWorkflow(_id = id.value, userId = userId, name = name, settings = settings)
			.also {
				flowScheduler.schedule(it)
			}
	}

	override fun query(userId: UserId): Flow<StandaloneWorkflow> = db.flowTransaction {
		flowTable.selectStandalone()
			.where { flowTable.userId eq userId }
			.map { it.toStandalone(flowTable) }
	}

	override suspend fun getStandalone(flowId: FlowId): StandaloneWorkflow? = db.suspendTransaction {
		flowTable.selectStandalone()
			.where { flowTable.id eq flowId }
			.firstOrNull()
			?.toStandalone(flowTable)
	}

	override suspend fun getWithNodes(flowId: FlowId): WorkflowWithNodes? = db.suspendTransaction {
		val flow = flowTable.selectStandalone()
			.where { flowTable.id eq flowId }
			.firstOrNull() ?: return@suspendTransaction null

		val nodes = nodeService.getByFlow(flowId).toList()

		flow.let {
			WorkflowWithNodes(
				_id = it[flowTable.id].value,
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

	override suspend fun rename(flowId: FlowId, name: String) = db.suspendTransaction<Unit> {
		flowTable.update({ flowTable.id eq flowId }) {
			it[flowTable.name] = name
		}
	}

	override suspend fun updateSettings(flowId: FlowId, settings: WorkflowSettings) = db.suspendTransaction {
		val flow = flowTable.updateReturning(flowTable.standaloneColumns, { flowTable.id eq flowId }) {
			it[flowTable.settings] = settings
		}
			.first()
			.toStandalone(flowTable)

		flowScheduler.reschedule(flow)
	}

	override suspend fun delete(flowId: FlowId) = db.suspendTransaction<Unit> {
		flowTable.deleteWhere { id eq flowId }
	}
}
