package me.snoty.integration.moodle.calendar

import io.ktor.server.routing.*
import kotlinx.datetime.toJavaLocalDateTime
import me.snoty.integration.common.diff.Fields
import me.snoty.integration.moodle.MoodleEntityStateTable
import me.snoty.integration.moodle.MoodleIntegration
import me.snoty.integration.moodle.model.MoodleAssignment
import me.snoty.integration.utils.calendar.ICalBuilder
import me.snoty.integration.utils.calendar.calendarRoutes
import net.fortuna.ical4j.model.component.VEvent

object MoodleCalendarBuilder : ICalBuilder<Long>(MoodleEntityStateTable) {
	override fun buildEvent(id: Long, fields: Fields): VEvent {
		val exam = MoodleAssignment.fromFields(id, fields)
		val due = exam.due.toJavaLocalDateTime()
		return VEvent(due, due, exam.name)
	}
}

fun Route.iCalRoutes() {
	calendarRoutes(
		MoodleIntegration.INTEGRATION_NAME,
		MoodleAssignment.TYPE,
		MoodleCalendarBuilder
	)
}
