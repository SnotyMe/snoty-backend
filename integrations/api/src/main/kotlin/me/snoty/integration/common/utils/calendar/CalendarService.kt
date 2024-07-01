package me.snoty.integration.common.utils.calendar

import me.snoty.backend.integration.config.flow.NodeId

interface CalendarService {
	suspend fun create(nodeId: NodeId, calType: String): CalendarId
	suspend fun get(calendarID: CalendarId): NodeId?
}
