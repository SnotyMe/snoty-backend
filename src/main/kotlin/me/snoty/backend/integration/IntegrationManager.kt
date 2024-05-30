package me.snoty.backend.integration

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.spi.IntegrationRegistry
import me.snoty.integration.common.*
import me.snoty.integration.common.diff.EntityStateService


class IntegrationManager(
	scheduler: Scheduler,
	entityStateServiceFactory: (IntegrationDescriptor) -> EntityStateService
) {
	private val logger = KotlinLogging.logger {}

	val integrations: List<Integration> = IntegrationRegistry.getIntegrationFactories().map {
		val context = IntegrationContext(
			entityStateServiceFactory(it.descriptor),
			scheduler
		)
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
