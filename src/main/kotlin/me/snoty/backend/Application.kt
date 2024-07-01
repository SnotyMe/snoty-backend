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
import me.snoty.backend.integration.config.MongoNodeService
import me.snoty.backend.integration.flow.FlowRunnerImpl
import me.snoty.backend.integration.flow.MongoFlowService
import me.snoty.backend.integration.flow.node.NodeRegistryImpl
import me.snoty.backend.logging.setupLogbackFilters
import me.snoty.backend.scheduling.JobRunrConfigurer
import me.snoty.backend.server.KtorServer
import me.snoty.backend.spi.DevManager

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
	// TODO: use again
	// val metricsPool = Executors.newScheduledThreadPool(1)
	// val scheduler = JobRunrScheduler()

	val (mongoDB, syncMongoClient) = createMongoClients(config.mongodb)

	val nodeRegistry = NodeRegistryImpl()
	val flowService = MongoFlowService(mongoDB, FlowRunnerImpl(nodeRegistry))

	val nodeService = MongoNodeService(mongoDB, nodeRegistry)
	// TODO: use
	// val calendarService = MongoCalendarService(mongoDB)

	JobRunrConfigurer.configure(syncMongoClient, nodeRegistry, nodeService, flowService, meterRegistry)

	KtorServer(config, buildInfo, meterRegistry, nodeRegistry, flowService, nodeService)
		.start(wait = true)
}
