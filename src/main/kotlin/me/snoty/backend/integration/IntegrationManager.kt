package me.snoty.backend.integration
/*
class IntegrationManager(
	scheduler: Scheduler,
	private val nodeService: NodeService,
	calendarService: CalendarService,
	entityStateServiceFactory: (NodeDescriptor) -> EntityStateService
) {
	private val logger = KotlinLogging.logger {}
	val integrations: List<Integration> = IntegrationRegistry.getIntegrationFactories().map {
		val context = NodeContext(
			entityStateServiceFactory(it.descriptor),
			nodeService,
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
}
*/
