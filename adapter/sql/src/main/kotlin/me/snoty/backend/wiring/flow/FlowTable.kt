package me.snoty.backend.wiring.flow

import me.snoty.backend.database.sql.utils.UuidTable
import me.snoty.backend.database.sql.utils.kotlinUuid
import me.snoty.integration.common.wiring.flow.StandaloneWorkflow
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.koin.core.annotation.Single
import kotlin.uuid.toJavaUuid

object FlowTable : UuidTable("flow") {
	val userId = kotlinUuid("user_id")
	val name = varchar("name", 255)

	fun selectStandalone() = FlowTable.select(id, userId, name)
}

@Single(binds = [Table::class])
fun provideFlowTable() = FlowTable

fun ResultRow.toStandalone() = StandaloneWorkflow(
	_id = this[FlowTable.id].value.toString(),
	userId = this[FlowTable.userId].toJavaUuid(),
	name = this[FlowTable.name]
)
