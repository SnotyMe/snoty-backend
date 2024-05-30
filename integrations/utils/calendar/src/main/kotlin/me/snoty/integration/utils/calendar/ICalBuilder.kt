package me.snoty.integration.utils.calendar

import me.snoty.integration.common.InstanceId
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.Fields
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion
import java.util.*

data class CalendarConfig(
	val userId: UUID,
	val instanceId: InstanceId,
	val type: String
)

abstract class ICalBuilder<ID>(private val entityStateService: EntityStateService) {
	protected abstract fun buildEvent(id: String, fields: Fields): VEvent

	suspend fun build(calendarConfig: CalendarConfig): Calendar {
		val userId = calendarConfig.userId
		val instanceId = calendarConfig.instanceId
		val type = calendarConfig.type
		val rows = entityStateService.getEntities(userId, instanceId, type)
		val calendar = Calendar()
		calendar.add<Calendar>(ImmutableVersion.VERSION_2_0)

		rows.collect { row ->
			val state = row.state
			val id = row.id
			calendar.add<Calendar>(buildEvent(id, state))
		}

		return calendar
	}
}
