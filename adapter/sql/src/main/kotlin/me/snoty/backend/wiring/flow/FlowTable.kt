package me.snoty.backend.wiring.flow

import kotlinx.serialization.json.Json
import me.snoty.backend.database.sql.utils.UuidTable
import me.snoty.backend.database.sql.utils.kotlinUuid
import me.snoty.integration.common.wiring.flow.StandaloneWorkflow
import me.snoty.integration.common.wiring.flow.WorkflowSettings
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import org.koin.core.annotation.Single

@Single(binds = [Table::class])
class FlowTable(json: Json) : UuidTable("flow") {
	val userId = kotlinUuid("user_id")
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
