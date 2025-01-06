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
import me.snoty.integration.common.wiring.flow.WorkflowWithNodes
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.koin.core.annotation.Single
import java.util.*
import kotlin.uuid.toJavaUuid
import kotlin.uuid.toKotlinUuid

@Single
class SqlFlowService(
	private val db: Database,
	private val flowScheduler: FlowScheduler,
	private val nodeService: NodeService,
) : FlowService {
	override suspend fun create(userId: UUID, name: String): StandaloneWorkflow = db.newSuspendedTransaction {
		val id = FlowTable.insertAndGetId {
			it[FlowTable.userId] = userId.toKotlinUuid()
			it[FlowTable.name] = name
		}

		StandaloneWorkflow(_id = id.value.toString(), userId = userId, name = name)
			.also {
				flowScheduler.schedule(it)
			}
	}

	override fun query(userId: UUID): Flow<StandaloneWorkflow> = db.flowTransaction {
		FlowTable.selectStandalone()
			.where { FlowTable.userId eq userId.toKotlinUuid() }
			.map(ResultRow::toStandalone)
	}

	override suspend fun getStandalone(flowId: NodeId): StandaloneWorkflow? = db.newSuspendedTransaction {
		FlowTable.selectStandalone()
			.where { FlowTable.id eq flowId.toUuid() }
			.firstOrNull()
			?.toStandalone()
	}

	override suspend fun getWithNodes(flowId: NodeId): WorkflowWithNodes? = db.newSuspendedTransaction {
		val flow = FlowTable.select(FlowTable.id, FlowTable.userId, FlowTable.name)
			.where { FlowTable.id eq flowId.toUuid() }
			.firstOrNull() ?: return@newSuspendedTransaction null

		val nodes = nodeService.getByFlow(flowId).toList()

		flow.let {
			WorkflowWithNodes(
				_id = it[FlowTable.id].value.toString(),
				userId = it[FlowTable.userId].toJavaUuid(),
				name = it[FlowTable.name],
				nodes = nodes,
			)
		}
	}

	override fun getAll(): Flow<StandaloneWorkflow> = db.flowTransaction {
		FlowTable.selectStandalone().map(ResultRow::toStandalone)
	}

	override suspend fun rename(flowId: NodeId, name: String) = db.newSuspendedTransaction<Unit> {
		FlowTable.update({ FlowTable.id eq flowId.toUuid() }) {
			it[FlowTable.name] = name
		}
	}

	override suspend fun delete(flowId: NodeId) = db.newSuspendedTransaction<Unit> {
		FlowTable.deleteWhere { id eq flowId.toUuid() }
	}
}
