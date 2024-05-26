package me.snoty.integration.moodle.calendar

import io.ktor.server.routing.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import me.snoty.integration.common.diff.Fields
import me.snoty.integration.moodle.MoodleIntegration
import me.snoty.integration.moodle.MoodleStateCollection
import me.snoty.integration.moodle.model.MoodleAssignment
import me.snoty.integration.utils.calendar.ICalBuilder
import me.snoty.integration.utils.calendar.calendarRoutes
import net.fortuna.ical4j.model.component.VEvent

class MoodleCalendarBuilder(moodleStateCollection: MoodleStateCollection) : ICalBuilder<Long>(moodleStateCollection) {
	override fun buildEvent(id: String, fields: Fields): VEvent {
		val exam = MoodleAssignment.fromFields(id.toLong(), fields)
		val due = exam.due.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime()
		return VEvent(due, due, exam.name)
	}
}

fun Route.iCalRoutes(stateCollection: MoodleStateCollection) {
	calendarRoutes(
		MoodleIntegration.INTEGRATION_NAME,
		MoodleAssignment.TYPE,
		MoodleCalendarBuilder(stateCollection)
	)
}
