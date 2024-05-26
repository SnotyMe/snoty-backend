package me.snoty.integration.utils.calendar

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import me.snoty.backend.database.mongo.Aggregations
import me.snoty.backend.database.mongo.aggregate
import me.snoty.integration.common.InstanceId
import me.snoty.integration.common.diff.state.EntityState
import me.snoty.integration.common.diff.state.EntityStateCollection
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

abstract class ICalBuilder<ID>(private val entityStateCollection: EntityStateCollection) {
	protected abstract fun buildEvent(id: String, fields: Fields): VEvent

	suspend fun build(calendarConfig: CalendarConfig): Calendar {
		val userId = calendarConfig.userId
		val instanceId = calendarConfig.instanceId
		val type = calendarConfig.type
		val rows = entityStateCollection.aggregate<EntityState>(
			Aggregates.match(Filters.eq("_id", userId)),
			Aggregations.project(
				Projections.exclude("_id"),
				Projections.computed("entity", "\$entities.$instanceId")
			),
			Aggregates.unwind("entity"),
			Aggregates.match(Filters.eq("entity.type", type))
		)
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
