package me.snoty.backend.wiring.flow

import kotlinx.serialization.json.Json
import me.snoty.backend.database.sql.utils.UuidTable
import me.snoty.integration.common.wiring.flow.StandaloneWorkflow
import me.snoty.integration.common.wiring.flow.WorkflowSettings
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.json.jsonb
import org.koin.core.annotation.Single

@Single(binds = [Table::class])
class FlowTable(json: Json) : UuidTable("flow") {
	val userId = uuid("user_id")
	val name = varchar("name", 255)
	val settings = jsonb<WorkflowSettings>("settings", json).nullable() // nullable for backwards compatibility

	val standaloneColumns = listOf(id, userId, name, settings)
	fun selectStandalone() = select(standaloneColumns)
}

fun ResultRow.toStandalone(flowTable: FlowTable) = StandaloneWorkflow(
	_id = this[flowTable.id].value.toString(),
	userId = this[flowTable.userId],
	name = this[flowTable.name],
	settings = this[flowTable.settings] ?: WorkflowSettings(),
)
