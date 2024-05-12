package me.snoty.integration.untis.calendar

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object WebUntisCalendar : IdTable<UUID>() {
	override val id: Column<EntityID<UUID>> = uuid("id").autoGenerate().entityId()

	val instanceId = integer("instance_id")
	val userId = uuid("user_id")
	val type = varchar("type", 255)

	init {
		transaction {
			SchemaUtils.createMissingTablesAndColumns(this@WebUntisCalendar)
		}
	}

	fun create(userId: UUID, instanceId: Int, type: String) = transaction {
		insertAndGetId {
			it[this.userId] = userId
			it[this.instanceId] = instanceId
			it[this.type] = type
		}.value
	}

	fun get(calendarId: UUID, type: String) = transaction {
		selectAll()
			.where { WebUntisCalendar.id eq calendarId and (WebUntisCalendar.type eq type) }
			.firstOrNull()
	}
}
