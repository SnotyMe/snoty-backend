package me.snoty.integration.common

import me.snoty.backend.scheduling.Scheduler
import me.snoty.integration.common.diff.EntityStateService

data class IntegrationContext(
	val entityStateService: EntityStateService,
	val scheduler: Scheduler
)
