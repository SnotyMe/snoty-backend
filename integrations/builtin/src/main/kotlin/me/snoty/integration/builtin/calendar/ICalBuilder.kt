package me.snoty.integration.builtin.calendar

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.toJavaInstant
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.Name
import net.fortuna.ical4j.model.property.ProdId
import net.fortuna.ical4j.model.property.Uid
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion
import org.koin.core.annotation.Single

@Single
class ICalBuilder(private val config: ICalConfig) {
	suspend fun build(calendarId: String, name: String, events: Flow<CalendarEvent>): Calendar {
		val calendar = Calendar()
		calendar.add<Calendar>(ProdId("-//Snoty//Snoty Calendar @ ${config.domain}//EN"))
		calendar.add<Calendar>(Name(name))
		calendar.add<Calendar>(Uid("${calendarId}@${config.domain}"))
		calendar.add<Calendar>(ImmutableVersion.VERSION_2_0)

		events.collect { row ->
			calendar.add<Calendar>(
				VEvent(
					(row.startDate ?: row.date)?.toJavaInstant(),
					(row.endDate ?: row.date)?.toJavaInstant(),
					row.name,
				).add<VEvent>(Uid("${row.id}-${calendarId}@${config.domain}"))
			)
		}

		return calendar
	}
}
