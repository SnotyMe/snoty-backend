package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.newSuspendedTransaction
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.utils.toUuid
import me.snoty.backend.wiring.flow.FlowTable
import me.snoty.integration.common.wiring.flow.EnumeratedFlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.NodeLogEntry
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.koin.core.annotation.Single
import java.util.*
import kotlin.uuid.toKotlinUuid

@Single
class SqlFlowExecutionService(
	private val db: Database,
	private val flowTable: FlowTable,
	private val flowExecutionTable: FlowExecutionTable,
	private val flowExecutionLogTable: FlowExecutionLogTable,
) : FlowExecutionService {
	override suspend fun create(jobId: String, flowId: NodeId, triggeredBy: FlowTriggerReason) = db.newSuspendedTransaction<Unit> {
		flowExecutionTable.upsert {
			it[id] = jobId
			it[this.flowId] = flowId.toUuid()
			it[this.triggeredBy] = triggeredBy
			it[this.triggeredAt] = Clock.System.now()
			it[this.status] = FlowExecutionStatus.RUNNING
		}.execute(it)
	}

	override suspend fun record(jobId: String, entry: NodeLogEntry) = db.newSuspendedTransaction<Unit> {
		flowExecutionLogTable.insert {
			it[executionId] = jobId
			it[timestamp] = entry.timestamp
			it[level] = entry.level
			it[message] = entry.message
			it[node] = entry.node
		}
	}

	override suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus) = db.newSuspendedTransaction<Unit> {
		flowExecutionTable.upsert(where = { flowExecutionTable.id eq jobId }) {
			it[this.status] = status
		}.execute(it)
	}

	override suspend fun retrieve(flowId: NodeId): List<NodeLogEntry> = db.newSuspendedTransaction {
		flowExecutionLogTable.selectAll()
			.where { flowExecutionLogTable.executionId eq flowId }
			.orderBy(flowExecutionLogTable.timestamp to SortOrder.DESC)
			.map { it.toLogEntry(flowExecutionLogTable) }
	}

	override fun query(userId: UUID): Flow<EnumeratedFlowExecution> = db.flowTransaction {
		flowExecutionTable
			.joinQuery(on = {
				flowExecutionTable.flowId eq flowExecutionTable.alias
			}) {
				flowExecutionTable.select(flowExecutionTable.flowId.alias("last_flow_id"), flowExecutionTable.triggeredAt.max().alias("last_triggered_at"))
					.where { flowExecutionTable.flowId inSubQuery flowTable.select(flowTable.id).where { flowTable.userId eq userId.toKotlinUuid() } }
					.groupBy(flowExecutionTable.flowId)
			}
			.selectAll()
			.map {
				EnumeratedFlowExecution(
					jobId = it[flowExecutionTable.id].value,
					flowId = it[flowExecutionTable.flowId].value.toString(),
					triggeredBy = it[flowExecutionTable.triggeredBy],
					startDate = it[flowExecutionTable.triggeredAt],
					status = it[flowExecutionTable.status]
				)
			}
	}

	override fun query(
		flowId: NodeId,
		startFrom: String?,
		limit: Int
	): Flow<FlowExecution> = db.flowTransaction {
		val executions = flowExecutionTable.selectAll()
			.where {
				(flowExecutionTable.flowId eq flowId.toUuid()) andIfNotNull (startFrom?.let { flowExecutionTable.id less it })
			}
			.orderBy(flowExecutionTable.triggeredAt to SortOrder.DESC)
			.limit(limit)
			.toList()

		val logs = flowExecutionLogTable.selectAll()
			.where { flowExecutionLogTable.executionId inList executions.map { it[flowExecutionTable.id] } }
			.orderBy(flowExecutionLogTable.timestamp to SortOrder.DESC)
			.toList()
			.groupBy { it[flowExecutionLogTable.executionId].value }

		executions.map { execution ->
			FlowExecution(
				jobId = execution[flowExecutionTable.id].value,
				flowId = execution[flowExecutionTable.flowId].value.toString(),
				triggeredBy = execution[flowExecutionTable.triggeredBy],
				startDate = execution[flowExecutionTable.triggeredAt],
				status = execution[flowExecutionTable.status],
				logs = logs[execution[flowExecutionTable.id].value]?.map {
					it.toLogEntry(flowExecutionLogTable)
				}
			)
		}
	}

	override suspend fun deleteAll(flowId: NodeId) = db.newSuspendedTransaction<Unit> {
		flowExecutionTable.deleteWhere { flowExecutionTable.flowId eq flowId.toUuid() }
	}
}
