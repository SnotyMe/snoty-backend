package me.snoty.integration.utils.calendar

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class CalendarTable(integrationName: String) : IdTable<UUID>("${integrationName}calendar") {
	override val id = uuid("id").autoGenerate().entityId()

	val instanceId = integer("instance_id")
	val userId = uuid("user_id")
	val type = varchar("type", 255)

	init {
		transaction {
			SchemaUtils.createMissingTablesAndColumns(this@CalendarTable)
		}
	}

	fun create(userId: UUID, instanceId: Int, type: String) = transaction {
		insertAndGetId {
			it[this@CalendarTable.userId] = userId
			it[this@CalendarTable.instanceId] = instanceId
			it[this@CalendarTable.type] = type
		}.value
	}

	fun get(calendarId: UUID, type: String) = transaction {
		selectAll()
			.where { this@CalendarTable.id eq calendarId and (this@CalendarTable.type eq type) }
			.firstOrNull()
	}
}
