package me.snoty.backend.integration.utils.calendar

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.utils.calendar.CalendarId
import org.bson.codecs.pojo.annotations.BsonId

data class CalendarSettings(
	val nodeId: NodeId,
	val type: String,
	@BsonId
	val id: CalendarId = CalendarId()
)
