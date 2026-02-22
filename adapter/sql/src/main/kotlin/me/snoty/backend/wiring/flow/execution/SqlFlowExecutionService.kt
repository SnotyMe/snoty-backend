package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.Flow
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.suspendTransaction
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.wiring.flow.FlowTable
import me.snoty.core.FlowId
import me.snoty.core.UserId
import me.snoty.integration.common.wiring.flow.EnumeratedFlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecution
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.NodeLogEntry
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.koin.core.annotation.Single
import kotlin.time.Clock

@Single
class SqlFlowExecutionService(
	private val db: Database,
	private val flowTable: FlowTable,
	private val flowExecutionTable: FlowExecutionTable,
	private val flowExecutionLogTable: FlowExecutionLogTable,
) : FlowExecutionService {
	override suspend fun create(jobId: String, flowId: FlowId, triggeredBy: FlowTriggerReason) = db.suspendTransaction<Unit> {
		flowExecutionTable.upsert {
			it[id] = jobId
			it[this.flowId] = flowId
			it[this.triggeredBy] = triggeredBy
			it[this.triggeredAt] = Clock.System.now()
			it[this.status] = FlowExecutionStatus.RUNNING
		}
	}

	override suspend fun record(jobId: String, entry: NodeLogEntry) = db.suspendTransaction<Unit> {
		flowExecutionLogTable.insert {
			it[executionId] = jobId
			it[timestamp] = entry.timestamp
			it[level] = entry.level
			it[message] = entry.message
			it[node] = entry.node
		}
	}

	override suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus) = db.suspendTransaction<Unit> {
		flowExecutionTable.update(where = { flowExecutionTable.id eq jobId }) {
			it[this.status] = status
		}
	}

	override suspend fun retrieve(flowId: FlowId): List<NodeLogEntry> = db.suspendTransaction {
		flowExecutionLogTable.selectAll()
			.where { flowExecutionLogTable.executionId eq flowId.value }
			.orderBy(flowExecutionLogTable.timestamp to SortOrder.DESC)
			.map { it.toLogEntry(flowExecutionLogTable) }
	}

	override fun query(userId: UserId): Flow<EnumeratedFlowExecution> = db.flowTransaction {
		val lastFlowId = flowExecutionTable.flowId.alias("last_flow_id")
		val lastTriggeredAt = flowExecutionTable.triggeredAt.max().alias("last_triggered_at")
		flowExecutionTable
			.joinQuery(on = {
				flowExecutionTable.flowId eq lastFlowId.aliasOnlyExpression() and (flowExecutionTable.triggeredAt eq lastTriggeredAt.aliasOnlyExpression())
			}) {
				flowExecutionTable.select(lastFlowId, lastTriggeredAt)
					.where { flowExecutionTable.flowId inSubQuery flowTable.select(flowTable.id).where { flowTable.userId eq userId } }
					.groupBy(flowExecutionTable.flowId)
			}
			.selectAll()
			.map {
				EnumeratedFlowExecution(
					jobId = it[flowExecutionTable.id].value,
					flowId = it[flowExecutionTable.flowId].value,
					triggeredBy = it[flowExecutionTable.triggeredBy],
					startDate = it[flowExecutionTable.triggeredAt],
					status = it[flowExecutionTable.status]
				)
			}
	}

	override fun query(
		flowId: FlowId,
		startFrom: String?,
		limit: Int
	): Flow<FlowExecution> = db.flowTransaction {
		val executions = flowExecutionTable.selectAll()
			.where {
				(flowExecutionTable.flowId eq flowId) andIfNotNull (startFrom?.let { flowExecutionTable.id less it })
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
				flowId = execution[flowExecutionTable.flowId].value,
				triggeredBy = execution[flowExecutionTable.triggeredBy],
				startDate = execution[flowExecutionTable.triggeredAt],
				status = execution[flowExecutionTable.status],
				logs = logs[execution[flowExecutionTable.id].value]?.map {
					it.toLogEntry(flowExecutionLogTable)
				} ?: emptyList()
			)
		}
	}

	override suspend fun deleteAll(flowId: FlowId) = db.suspendTransaction<Unit> {
		flowExecutionTable.deleteWhere { flowExecutionTable.flowId eq flowId }
	}
}
