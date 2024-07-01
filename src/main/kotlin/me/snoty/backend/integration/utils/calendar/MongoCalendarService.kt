package me.snoty.backend.integration.utils.calendar

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.utils.calendar.CalendarId
import me.snoty.integration.common.utils.calendar.CalendarService

class MongoCalendarService(mongoDB: MongoDatabase) : CalendarService {
	private val collection = mongoDB.getCollection<CalendarSettings>("calendar")

	override suspend fun create(nodeId: NodeId, calType: String): CalendarId {
		val filter = Filters.eq(CalendarSettings::nodeId.name, nodeId)
		val existingResult = collection.find(filter).firstOrNull()

		if (existingResult != null) {
			return existingResult.id
		}

		val calendarSetting = CalendarSettings(
			nodeId = nodeId,
			type = calType
		)
		collection.insertOne(calendarSetting)
		return calendarSetting.id
	}

	override suspend fun get(calendarID: CalendarId): NodeId? {
		val filter = Filters.eq(CalendarSettings::id.name, calendarID)
		return collection.find(filter).firstOrNull()?.nodeId
	}
}
