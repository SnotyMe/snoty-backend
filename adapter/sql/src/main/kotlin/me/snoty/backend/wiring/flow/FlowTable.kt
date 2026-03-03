package me.snoty.backend.wiring.flow

import kotlinx.serialization.json.Json
import me.snoty.backend.database.sql.utils.flowId
import me.snoty.backend.database.sql.utils.userId
import me.snoty.backend.utils.randomV7
import me.snoty.core.FlowId
import me.snoty.integration.common.wiring.flow.StandaloneWorkflow
import me.snoty.integration.common.wiring.flow.WorkflowSettings
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.json.jsonb
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single(binds = [Table::class])
class FlowTable(json: Json) : IdTable<FlowId>("flow") {
	override val id = flowId("id").clientDefault {
		FlowId(Uuid.randomV7().toString())
	}.entityId()
	override val primaryKey = PrimaryKey(id)

	val userId = userId("user_id")
	val name = text("name")
	val settings = jsonb<WorkflowSettings>("settings", json).nullable() // nullable for backwards compatibility

	val standaloneColumns = listOf(id, userId, name, settings)
	fun selectStandalone() = select(standaloneColumns)
}

fun ResultRow.toStandalone(flowTable: FlowTable) = StandaloneWorkflow(
	_id = this[flowTable.id].value,
	userId = this[flowTable.userId],
	name = this[flowTable.name],
	settings = this[flowTable.settings] ?: WorkflowSettings(),
)
