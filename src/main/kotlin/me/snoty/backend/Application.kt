package me.snoty.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.runBlocking
import me.snoty.backend.build.DevBuildInfo
import me.snoty.backend.config.ConfigLoaderImpl
import me.snoty.backend.config.ProviderFeatureFlagConfig
import me.snoty.backend.database.mongo.createMongoClients
import me.snoty.backend.featureflags.FeatureFlags
import me.snoty.backend.featureflags.FeatureFlagsSetup
import me.snoty.backend.featureflags.provider.FlagdProvider
import me.snoty.backend.integration.IntegrationManager
import me.snoty.backend.integration.MongoEntityStateService
import me.snoty.backend.integration.config.MongoIntegrationConfigService
import me.snoty.backend.integration.utils.calendar.MongoCalendarService
import me.snoty.backend.logging.setupLogbackFilters
import me.snoty.backend.scheduling.JobRunrConfigurer
import me.snoty.backend.scheduling.JobRunrScheduler
import me.snoty.backend.server.KtorServer
import me.snoty.backend.spi.DevManager
import java.util.concurrent.Executors

fun main() = runBlocking {
	setupLogbackFilters()
	val logger = KotlinLogging.logger {}

	// ran pre-config load to allow dev functions to configure the environment
	DevManager.runDevFunctions()

	val configLoader = ConfigLoaderImpl()
	val config = configLoader.loadConfig()
	logger.info { "Loaded config: $config" }

	val buildInfo = try {
		configLoader.loadBuildInfo()
	} catch (e: Exception) {
		// when ran without gradle, the build info is not available
		// it'll just default to dev build info in this case
		if (config.environment.isDev()) {
			logger.warn { "Failed to load build info: ${e.message}" }
			DevBuildInfo
		} else {
			throw e
		}
	}
	val featureClient = when (val featureFlagsConfig = config.featureFlags.value) {
			is ProviderFeatureFlagConfig.Flagd -> FlagdProvider.createClient(featureFlagsConfig)
			else -> throw IllegalArgumentException("No provider found for ${config.featureFlags.value}")
		}
	val featureFlags = FeatureFlags(config, featureClient)
	FeatureFlagsSetup.setup(featureClient, featureFlags)

	val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
	val metricsPool = Executors.newScheduledThreadPool(1)
	val scheduler = JobRunrScheduler()

	val (mongoDB, syncMongoClient) = createMongoClients(config.mongodb)

	val integrationConfigService = MongoIntegrationConfigService(mongoDB)
	val calendarService = MongoCalendarService(mongoDB)
	val integrationManager = IntegrationManager(scheduler, integrationConfigService, calendarService) { integrationDescriptor ->
		MongoEntityStateService(mongoDB, integrationDescriptor, meterRegistry, metricsPool)
	}

	JobRunrConfigurer.configure(syncMongoClient, integrationManager, meterRegistry)
	integrationManager.startup()

	KtorServer(config, buildInfo, meterRegistry, integrationManager)
		.start(wait = true)
}
