package me.snoty.integration.common.utils.calendar

import me.snoty.integration.common.InstanceId
import java.util.*

data class CalendarConfig(
	val userId: UUID,
	val instanceId: InstanceId,
	val type: String
)
