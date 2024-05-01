package me.snoty.backend.integration

import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.integration.common.Integration
import me.snoty.backend.integration.common.IntegrationContext
import me.snoty.backend.integration.moodle.MoodleIntegration
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.Executors


class IntegrationManager(database: Database, metricsRegistry: MeterRegistry) {
	private val metricsPool = Executors.newScheduledThreadPool(1)
	private val context = IntegrationContext(database, metricsRegistry, metricsPool)

	// TODO: replace with SPI
	val integrations: List<Integration> = listOf(
		MoodleIntegration(context)
	)

	fun startup() {
		integrations.forEach(Integration::start)
	}

	fun <T> getScheduleHandler(type: Class<T>): T? {
		@Suppress("UNCHECKED_CAST")
		return integrations.find {
			it.scheduler.javaClass == type
			|| it.scheduler.javaClass == type.enclosingClass
		}?.scheduler as T
	}
}
