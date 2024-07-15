package me.snoty.integration.utils.calendar

import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.diff.Fields
import me.snoty.integration.common.wiring.Node
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion

abstract class ICalBuilder<ID>(private val entityStateService: EntityStateService) {
	protected abstract fun buildEvent(id: String, fields: Fields): VEvent

	suspend fun build(node: Node): Calendar {
		val rows = entityStateService.getStates(node)
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
