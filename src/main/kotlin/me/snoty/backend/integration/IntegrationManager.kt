package me.snoty.backend.integration

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import me.snoty.backend.scheduling.Scheduler
import me.snoty.backend.spi.IntegrationRegistry
import me.snoty.backend.utils.NotFoundException
import me.snoty.integration.common.*
import me.snoty.integration.common.config.ConfigId
import me.snoty.integration.common.config.IntegrationConfigService
import me.snoty.integration.common.diff.EntityStateService
import me.snoty.integration.common.utils.calendar.CalendarService


class IntegrationManager(
	scheduler: Scheduler,
	private val integrationConfigService: IntegrationConfigService,
	calendarService: CalendarService,
	entityStateServiceFactory: (IntegrationDescriptor) -> EntityStateService
) {
	private val logger = KotlinLogging.logger {}

	val integrations: List<Integration> = IntegrationRegistry.getIntegrationFactories().map {
		val context = IntegrationContext(
			entityStateServiceFactory(it.descriptor),
			integrationConfigService,
			calendarService,
			scheduler
		)
		it.create(context)
	}

	suspend fun startup() = supervisorScope {
		logger.info { "Starting ${integrations.size} integrations..." }
		integrations.map {
			async {
				try {
					it.start()
				} catch (e: Exception) {
					logger.error(e) { "Failed to start integration ${it.name}" }
				}
			}
		}.awaitAll()
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

	fun getIntegration(integrationType: String): Integration? {
		return integrations.find {
			it.name == integrationType
		}
	}

	suspend fun getIntegrationConfig(configId: ConfigId, integrationType: String): IntegrationSettings? {
		val integration = getIntegration(integrationType) ?: throw NotFoundException("Integration of this type not found")
		return integrationConfigService.get(configId, integrationType, integration.settingsType)
	}
}
