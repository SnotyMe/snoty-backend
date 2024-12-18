package me.snoty.backend.wiring.flow.execution

import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.integration.common.wiring.flow.*
import org.bson.codecs.pojo.annotations.BsonId
import java.util.*

interface FlowExecutionService {
	suspend fun create(jobId: String, flowId: NodeId, triggeredBy: FlowTriggerReason)
	suspend fun record(jobId: String, entry: NodeLogEntry)
	suspend fun setExecutionStatus(jobId: String, status: FlowExecutionStatus)

	suspend fun retrieve(flowId: NodeId): List<NodeLogEntry>
	fun query(userId: UUID): Flow<EnumeratedFlowExecution>
	fun query(flowId: NodeId, startFrom: String?, limit: Int = 15): Flow<FlowExecution>

	suspend fun deleteAll(flowId: NodeId)
}

data class FlowLogs(
	@BsonId
	/**
	 * The execution / job ID. Every flow can have multiple executions.
	 */
	val _id: String,
	val flowId: NodeId,
	val triggeredBy: FlowTriggerReason?,
	val creationDate: Instant,
	val status: FlowExecutionStatus? = null,
	val logs: List<NodeLogEntry>,
)
