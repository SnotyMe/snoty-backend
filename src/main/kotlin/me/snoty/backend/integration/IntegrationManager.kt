package me.snoty.backend.integration

import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.integration.common.Integration
import me.snoty.backend.integration.common.IntegrationContext
import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.spi.IntegrationRegistry
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.Executors


class IntegrationManager(database: Database, metricsRegistry: MeterRegistry, scheduler: Scheduler) {
	private val metricsPool = Executors.newScheduledThreadPool(1)
	private val context = IntegrationContext(database, metricsRegistry, metricsPool, scheduler)

	val integrations: List<Integration> = IntegrationRegistry.getIntegrationFactories().map {
		it.create(context)
	}

	fun startup() {
		integrations.forEach(Integration::start)
	}

	fun <T> getScheduleHandler(type: Class<T>): T? {
		@Suppress("UNCHECKED_CAST")
		return integrations.find {
			it.fetcher.javaClass == type
			|| it.fetcher.javaClass == type.enclosingClass
		}?.fetcher as T
	}
}
