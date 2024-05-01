package me.snoty.backend.integration.common

import io.micrometer.core.instrument.MeterRegistry
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.ScheduledExecutorService

data class IntegrationContext(
	val database: Database,
	val meterRegistry: MeterRegistry,
	val metricsPool: ScheduledExecutorService
)
