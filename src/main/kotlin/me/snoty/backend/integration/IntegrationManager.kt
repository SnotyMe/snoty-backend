package me.snoty.backend.integration

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import me.snoty.integration.common.Integration
import me.snoty.integration.common.IntegrationContext
import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.spi.IntegrationRegistry
import me.snoty.integration.common.IntegrationConfigTable
import me.snoty.integration.common.IntegrationSettings
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.Executors


class IntegrationManager(
	database: Database,
	mongoDB: MongoDatabase,
	metricsRegistry: MeterRegistry,
	scheduler: Scheduler
) {
	private val metricsPool = Executors.newScheduledThreadPool(1)
	private val context = IntegrationContext(
		database,
		mongoDB,
		metricsRegistry,
		metricsPool,
		scheduler
	)
	private val logger = KotlinLogging.logger {}

	val integrations: List<Integration> = IntegrationRegistry.getIntegrationFactories().map {
		it.create(context)
	}

	fun startup() {
		logger.info { "Starting ${integrations.size} integrations..." }
		integrations.forEach {
			try {
				it.start()
			} catch (e: Exception) {
				logger.error(e) { "Failed to start integration ${it.name}" }
			}
		}
		logger.info { "Integration startup complete!" }
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

	fun getIntegrationConfig(configId: Long, integrationType: String): IntegrationSettings? {
		return IntegrationConfigTable.get(configId, integrationType)
	}
}
