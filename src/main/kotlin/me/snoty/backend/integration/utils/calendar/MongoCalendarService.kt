package me.snoty.backend.integration.utils.calendar

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import me.snoty.backend.database.mongo.Accumulations
import me.snoty.backend.database.mongo.aggregate
import me.snoty.backend.database.mongo.upsertOne
import me.snoty.integration.common.InstanceId
import me.snoty.integration.common.config.ConfigId
import me.snoty.integration.common.utils.calendar.CalendarConfig
import me.snoty.integration.common.utils.calendar.CalendarService
import java.util.*

class MongoCalendarService(mongoDB: MongoDatabase) : CalendarService {
	private val collection = mongoDB.getCollection<UserCalendarConfig>("integration_config.calendar")

	override suspend fun create(userID: UUID, instanceId: InstanceId, integrationType: String, calType: String): ConfigId {
		val filter = Filters.eq("_id", userID)
		val calendarSetting = CalendarSettings(instanceId = instanceId, type = calType)
		val update = Updates.push("configs.$integrationType", calendarSetting)
		collection.upsertOne(filter, update)

		return calendarSetting._id
	}

	override suspend fun get(calendarID: ConfigId, integrationType: String): CalendarConfig? {
		val path = "configs.$integrationType"
		val computedPath = "\$$path"
		val configFilter = Filters.eq("$path._id", calendarID)

		return collection.aggregate<CalendarConfig>(
			Aggregates.match(configFilter),
			Aggregates.unwind(computedPath),
			Aggregates.match(configFilter),
			Aggregates.replaceRoot(
				Accumulations.mergeObjects(
					computedPath,
					Projections.computed("userId", "\$_id")
				)
			)
		).firstOrNull()
	}
}
