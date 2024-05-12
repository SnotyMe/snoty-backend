package me.snoty.backend.integration

import io.micrometer.core.instrument.MeterRegistry
import me.snoty.integration.common.Integration
import me.snoty.integration.common.IntegrationContext
import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.spi.IntegrationRegistry
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors


class IntegrationManager(database: Database, metricsRegistry: MeterRegistry, scheduler: Scheduler) {
	private val metricsPool = Executors.newScheduledThreadPool(1)
	private val context = IntegrationContext(database, metricsRegistry, metricsPool, scheduler)
	private val logger = LoggerFactory.getLogger(IntegrationManager::class.java)

	val integrations: List<Integration> = IntegrationRegistry.getIntegrationFactories().map {
		it.create(context)
	}

	fun startup() {
		logger.info("Starting ${integrations.size} integrations...")
		integrations.forEach(Integration::start)
		logger.info("Integration startup complete!")
	}

	/**
	 * Looks up a Fetcher by its type.
	 * Scheduled tasks will need a fetcher to run on. JobRunr looks up required instance by their type.
	 */
	fun <T> getFetchHandler(type: Class<T>): T? {
		@Suppress("UNCHECKED_CAST")
		return integrations.find {
			it.fetcher.javaClass == type
			|| it.fetcher.javaClass == type.enclosingClass
		}?.fetcher as T
	}
}
