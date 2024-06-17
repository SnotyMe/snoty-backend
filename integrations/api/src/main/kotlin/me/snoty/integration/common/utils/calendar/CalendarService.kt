package me.snoty.integration.common.utils.calendar

import me.snoty.integration.common.InstanceId
import me.snoty.backend.integration.config.ConfigId
import java.util.*

interface CalendarService {
	suspend fun create(userID: UUID, instanceId: InstanceId, integrationType: String, calType: String): ConfigId
	suspend fun get(calendarID: ConfigId, integrationType: String): CalendarConfig?
}
