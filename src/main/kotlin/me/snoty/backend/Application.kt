package me.snoty.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.opentelemetry.api.GlobalOpenTelemetry
import kotlinx.coroutines.runBlocking
import me.snoty.backend.build.DevBuildInfo
import me.snoty.backend.config.ConfigLoaderImpl
import me.snoty.backend.config.ProviderFeatureFlagConfig
import me.snoty.backend.database.mongo.createMongoClients
import me.snoty.backend.featureflags.FeatureFlags
import me.snoty.backend.featureflags.FeatureFlagsSetup
import me.snoty.backend.featureflags.provider.FlagdProvider
import me.snoty.backend.integration.MongoEntityStateService
import me.snoty.backend.integration.config.MongoNodeService
import me.snoty.backend.integration.flow.FlowRunnerImpl
import me.snoty.backend.integration.flow.MongoFlowService
import me.snoty.backend.integration.flow.node.NodeRegistryImpl
import me.snoty.backend.integration.utils.calendar.MongoCalendarService
import me.snoty.backend.logging.setupLogbackFilters
import me.snoty.backend.observability.getTracer
import me.snoty.backend.scheduling.JobRunrConfigurer
import me.snoty.backend.scheduling.JobRunrScheduler
import me.snoty.backend.scheduling.node.NodeSchedulerImpl
import me.snoty.backend.server.KtorServer
import me.snoty.backend.spi.DevManager
import me.snoty.backend.wiring.node.NodeHandlerContributorLookup
import me.snoty.integration.common.wiring.NodeHandlerContext
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateDataMapper
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateDataMapper
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

	val openTelemetry = GlobalOpenTelemetry.get()

	val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
	val metricsPool = Executors.newScheduledThreadPool(1)
	val scheduler = JobRunrScheduler()
	val nodeScheduler = NodeSchedulerImpl(scheduler)

	logger.info { "Connecting to MongoDB..." }
	val (mongoDB, syncMongoClient) = createMongoClients(config.mongodb)
	logger.info { "Connected to MongoDB!" }
	val codecRegistry = mongoDB.codecRegistry

	val nodeRegistry = NodeRegistryImpl()
	val flowService = MongoFlowService(
		mongoDB,
		FlowRunnerImpl(
			nodeRegistry,
			featureFlags,
			openTelemetry.getTracer(FlowRunnerImpl::class)
		)
	)

	val nodeService = MongoNodeService(mongoDB, nodeRegistry, nodeScheduler)
	val calendarService = MongoCalendarService(mongoDB)
	val intermediateDataMapperRegistry = IntermediateDataMapperRegistry()
	intermediateDataMapperRegistry[BsonIntermediateData::class] = BsonIntermediateDataMapper(codecRegistry)
	intermediateDataMapperRegistry[SimpleIntermediateData::class] = SimpleIntermediateDataMapper

	NodeHandlerContributorLookup.executeContributors(nodeRegistry) { descriptor ->
		NodeHandlerContext(
			entityStateService = MongoEntityStateService(mongoDB, descriptor, meterRegistry, metricsPool),
			nodeService = nodeService,
			flowService = flowService,
			codecRegistry = codecRegistry,
			calendarService = calendarService,
			scheduler = scheduler,
			openTelemetry = openTelemetry,
			intermediateDataMapperRegistry = intermediateDataMapperRegistry
		)
	}

	JobRunrConfigurer.configure(syncMongoClient, nodeRegistry, nodeService, flowService, meterRegistry)

	KtorServer(config, featureFlags, buildInfo, meterRegistry, nodeRegistry, flowService, nodeService)
		.start(wait = true)
}
