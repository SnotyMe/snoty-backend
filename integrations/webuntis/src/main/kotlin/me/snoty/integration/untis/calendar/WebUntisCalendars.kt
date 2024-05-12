package me.snoty.integration.untis.calendar

import io.ktor.server.routing.*
import me.snoty.integration.common.diff.Fields
import me.snoty.integration.untis.WebUntisEntityStateTable
import me.snoty.integration.untis.WebUntisIntegration
import me.snoty.integration.untis.model.UntisExam
import me.snoty.integration.utils.calendar.ICalBuilder
import me.snoty.integration.utils.calendar.calendarRoutes
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.Description


object WebUntisCalendarBuilder : ICalBuilder<Int>(WebUntisEntityStateTable) {
	override fun buildEvent(id: Int, fields: Fields): VEvent {
		val exam = UntisExam.fromFields(id, fields)
		val event = VEvent(exam.startDateTime.dateTime, exam.endDateTime.dateTime, exam.name)
		event.add<VEvent>(Description(exam.text))

		return event
	}
}

fun Route.iCalRoutes() {
	calendarRoutes(
		WebUntisIntegration.INTEGRATION_NAME,
		UntisExam.TYPE,
		WebUntisCalendarBuilder
	)
}
