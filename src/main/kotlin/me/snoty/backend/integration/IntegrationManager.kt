package me.snoty.backend.integration

import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.integration.moodle.startMoodleIntegration
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.Executors


class IntegrationManager(private val database: Database, private val metricsRegistry: MeterRegistry) {
	private val metricsPool = Executors.newScheduledThreadPool(1)

	fun startup() {
		startMoodleIntegration(database, metricsRegistry, metricsPool)
	}
}
