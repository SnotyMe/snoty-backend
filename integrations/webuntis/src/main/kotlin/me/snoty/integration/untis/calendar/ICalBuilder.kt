package me.snoty.integration.untis.calendar

import me.snoty.integration.untis.WebUntisEntityStateTable
import me.snoty.integration.untis.model.UntisExam
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.Description
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
object ICalBuilder {
	fun build(calendarConfig: CalendarConfig): Calendar {
		val userId = calendarConfig.userId
		val instanceId = calendarConfig.instanceId
		val type = calendarConfig.type
		val rows = transaction {
			WebUntisEntityStateTable.selectAll()
				.where {
					WebUntisEntityStateTable.userId eq userId and (WebUntisEntityStateTable.instanceId eq instanceId) and (WebUntisEntityStateTable.type eq type)
				}
				.toList()
		}
		val calendar = Calendar()
		calendar.add<Calendar>(ImmutableVersion.VERSION_2_0)

		rows.forEach { row ->
			val state = row[WebUntisEntityStateTable.state]
			val exam = UntisExam.fromFields(row[WebUntisEntityStateTable.id], state)
			val event = VEvent(exam.startDateTime.dateTime, exam.endDateTime.dateTime, exam.name)
			event.add<VEvent>(Description(exam.text))

			calendar.add<Calendar>(event)
		}

		return calendar
	}
}
