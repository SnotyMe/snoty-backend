package me.snoty.backend.wiring.flow.execution

import me.snoty.backend.database.sql.utils.UuidTable
import me.snoty.backend.scheduling.FlowTriggerReason
import me.snoty.backend.wiring.flow.FlowTable
import me.snoty.integration.common.wiring.flow.FlowExecutionStatus
import me.snoty.integration.common.wiring.flow.NodeLogEntry
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.timestamp
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@Single(binds = [Table::class])
class FlowExecutionTable(flowTable: FlowTable) : IdTable<String>() {
	override val id = varchar("id", 255).entityId()
	override val primaryKey = PrimaryKey(id)

	val flowId = reference("flow_id", flowTable, onDelete = ReferenceOption.CASCADE)
	val triggeredBy = enumerationByName<FlowTriggerReason>("triggered_by", 10)
	val triggeredAt = timestamp("triggered_at")
	val status = enumerationByName<FlowExecutionStatus>("status", 15)
}

@Single(binds = [Table::class])
class FlowExecutionLogTable(flowExecutionTable: FlowExecutionTable) : UuidTable() {
	val executionId = reference("execution_id", flowExecutionTable, onDelete = ReferenceOption.CASCADE)

	val timestamp = timestamp("timestamp")
	val level = enumerationByName("level", 10, Level::class)
	val message = text("message")
	val node = varchar("node", 255).nullable()
}

fun ResultRow.toLogEntry(flowExecutionLogTable: FlowExecutionLogTable) = NodeLogEntry(
	timestamp = this[flowExecutionLogTable.timestamp],
	level = this[flowExecutionLogTable.level],
	message = this[flowExecutionLogTable.message],
	node = this[flowExecutionLogTable.node]
)
