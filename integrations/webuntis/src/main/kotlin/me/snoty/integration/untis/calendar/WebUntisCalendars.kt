package me.snoty.integration.untis.calendar

import io.ktor.server.routing.*
import me.snoty.integration.common.config.IntegrationConfigService
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.Fields
import me.snoty.integration.common.utils.calendar.CalendarService
import me.snoty.integration.untis.WebUntisIntegration
import me.snoty.integration.untis.model.UntisExam
import me.snoty.integration.utils.calendar.ICalBuilder
import me.snoty.integration.utils.calendar.calendarRoutes
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.Description


class WebUntisCalendarBuilder(entityStateService: EntityStateService) : ICalBuilder<Int>(entityStateService) {
	override fun buildEvent(id: String, fields: Fields): VEvent {
		val exam = UntisExam.fromFields(id.toInt(), fields)
		val event = VEvent(exam.startDateTime.toLocalDateTime(), exam.endDateTime.toLocalDateTime(), exam.name)
		event.add<VEvent>(Description(exam.text))

		return event
	}
}

fun Route.iCalRoutes(integrationConfigService: IntegrationConfigService, calendarService: CalendarService, entityStateService: EntityStateService) {
	calendarRoutes(
		integrationConfigService,
		calendarService,
		WebUntisIntegration.INTEGRATION_NAME,
		UntisExam.TYPE,
		WebUntisCalendarBuilder(entityStateService)
	)
}
