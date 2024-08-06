package me.snoty.integration.moodle.calendar

import io.ktor.server.routing.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.Fields
import me.snoty.integration.moodle.model.MoodleAssignment
import me.snoty.integration.common.utils.calendar.CalendarService
import me.snoty.integration.utils.calendar.ICalBuilder
import me.snoty.integration.utils.calendar.calendarRoutes
import net.fortuna.ical4j.model.component.VEvent

class MoodleCalendarBuilder(entityStateService: EntityStateService) : ICalBuilder<Long>(entityStateService) {
	override fun buildEvent(id: String, fields: Fields): VEvent {
		val exam = MoodleAssignment.fromFields(id.toLong(), fields)
		val due = exam.due.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime()
		return VEvent(due, due, exam.name)
	}
}

fun Routing.iCalRoutes(nodeService: NodeService, calendarService: CalendarService, entityStateService: EntityStateService) {
	calendarRoutes(
		nodeService,
		calendarService,
		MoodleAssignment.TYPE,
		MoodleCalendarBuilder(entityStateService)
	)
}
