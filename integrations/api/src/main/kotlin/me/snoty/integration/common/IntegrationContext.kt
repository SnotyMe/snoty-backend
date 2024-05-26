package me.snoty.integration.common

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.scheduling.Scheduler
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.ScheduledExecutorService

data class IntegrationContext(
	val database: Database,
	val mongodb: MongoDatabase,
	val meterRegistry: MeterRegistry,
	val metricsPool: ScheduledExecutorService,
	val scheduler: Scheduler
)
