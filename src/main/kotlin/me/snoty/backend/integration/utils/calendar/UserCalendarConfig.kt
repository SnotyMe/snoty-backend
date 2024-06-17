package me.snoty.backend.integration.utils.calendar

import me.snoty.integration.common.InstanceId
import me.snoty.backend.integration.config.ConfigId
import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

data class UserCalendarConfig(
	@BsonId
	val userID: UUID,
	val configs: Map<InstanceId, List<CalendarSettings>>
)

data class CalendarSettings(
	val instanceId: InstanceId,
	val type: String,
	@BsonId
	val _id: ConfigId = ConfigId()
)
