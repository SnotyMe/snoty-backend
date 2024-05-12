package me.snoty.integration.utils.calendar

import me.snoty.integration.common.diff.EntityStateTable
import me.snoty.integration.common.diff.Fields
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

data class CalendarConfig(
	val userId: UUID,
	val instanceId: Int,
	val type: String
)

abstract class ICalBuilder<ID>(private val stateTable: EntityStateTable<ID>) {
	protected abstract fun buildEvent(id: ID, fields: Fields): VEvent

	fun build(calendarConfig: CalendarConfig): Calendar {
		val userId = calendarConfig.userId
		val instanceId = calendarConfig.instanceId
		val type = calendarConfig.type
		val rows = transaction {
			stateTable.selectAll()
				.where {
					stateTable.userId eq userId and (stateTable.instanceId eq instanceId) and (stateTable.type eq type)
				}
				.toList()
		}
		val calendar = Calendar()
		calendar.add<Calendar>(ImmutableVersion.VERSION_2_0)

		rows.forEach { row ->
			val state = row[stateTable.state]
			val id = row[stateTable.id]
			calendar.add<Calendar>(buildEvent(id, state))
		}

		return calendar
	}
}
