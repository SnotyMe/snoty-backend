package me.snoty.integration.builtin.calendar

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.toJavaInstant
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion

object ICalBuilder {
	suspend fun build(events: Flow<CalendarEvent>): Calendar {
		val calendar = Calendar()
		calendar.add<Calendar>(ImmutableVersion.VERSION_2_0)

		events.collect { row ->
			calendar.add<Calendar>(
				VEvent(
					row.date.toJavaInstant(),
					row.date.toJavaInstant(),
					row.name,
				)
			)
		}

		return calendar
	}
}
