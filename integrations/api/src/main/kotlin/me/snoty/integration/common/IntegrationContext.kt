package me.snoty.integration.common

import me.snoty.backend.scheduling.Scheduler
import me.snoty.integration.common.config.IntegrationConfigService
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.utils.calendar.CalendarService

data class IntegrationContext(
	val entityStateService: EntityStateService,
	val integrationConfigService: IntegrationConfigService,
	val calendarService: CalendarService,
	val scheduler: Scheduler
)
