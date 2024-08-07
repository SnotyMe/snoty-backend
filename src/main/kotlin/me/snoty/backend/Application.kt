package me.snoty.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.opentelemetry.api.GlobalOpenTelemetry
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.modules.plus
import me.snoty.backend.build.DevBuildInfo
import me.snoty.backend.config.ConfigLoaderImpl
import me.snoty.backend.config.ProviderFeatureFlagConfig
import me.snoty.backend.database.mongo.createMongoClients
import me.snoty.backend.featureflags.FeatureFlags
import me.snoty.backend.featureflags.FeatureFlagsSetup
import me.snoty.backend.featureflags.provider.FlagdProvider
import me.snoty.backend.injection.ServicesContainerImpl
import me.snoty.backend.integration.MongoEntityStateService
import me.snoty.backend.integration.config.MongoNodeService
import me.snoty.backend.integration.flow.FlowBuilderImpl
import me.snoty.backend.integration.flow.FlowRunnerImpl
import me.snoty.backend.integration.flow.MongoFlowService
import me.snoty.backend.integration.flow.logging.MongoNodeLogService
import me.snoty.backend.integration.flow.node.NodeRegistryImpl
import me.snoty.backend.integration.utils.calendar.MongoCalendarService
import me.snoty.backend.integration.utils.mongoSettingsLookup
import me.snoty.backend.logging.setupLogbackFilters
import me.snoty.backend.observability.getTracer
import me.snoty.backend.scheduling.JobRunrConfigurer
import me.snoty.backend.scheduling.JobRunrScheduler
import me.snoty.backend.scheduling.node.NodeSchedulerImpl
import me.snoty.backend.server.KtorServer
import me.snoty.backend.spi.DevManager
import me.snoty.backend.wiring.node.NodeHandlerContributorLookup
import me.snoty.integration.common.snotyJson
import me.snoty.integration.common.wiring.NodeHandlerContext
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateDataMapper
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateDataMapper
import me.snoty.integration.common.wiring.node.serializersModule
import org.bson.codecs.configuration.CodecRegistry
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
	val nodeLogService = MongoNodeLogService(mongoDB)
	val settingsLookup = mongoSettingsLookup(nodeRegistry, codecRegistry)
	val flowBuilder = FlowBuilderImpl(settingsLookup)
	val flowRunner = FlowRunnerImpl(
		nodeRegistry,
		featureFlags,
		openTelemetry.getTracer(FlowRunnerImpl::class)
	)
	val flowService = MongoFlowService(
		mongoDB,
		flowBuilder,
		flowRunner
	)

	val nodeService = MongoNodeService(mongoDB, nodeRegistry, nodeScheduler, settingsLookup)
	val calendarService = MongoCalendarService(mongoDB)
	val intermediateDataMapperRegistry = IntermediateDataMapperRegistry()
	intermediateDataMapperRegistry[BsonIntermediateData::class] = BsonIntermediateDataMapper(codecRegistry)
	intermediateDataMapperRegistry[SimpleIntermediateData::class] = SimpleIntermediateDataMapper

	NodeHandlerContributorLookup.executeContributors(nodeRegistry) { descriptor ->
		val entityStateService = MongoEntityStateService(mongoDB, descriptor, meterRegistry, metricsPool)
		entityStateService.scheduleMetricsTask()
		NodeHandlerContext(
			entityStateService = entityStateService,
			nodeService = nodeService,
			flowService = flowService,
			codecRegistry = codecRegistry,
			calendarService = calendarService,
			scheduler = scheduler,
			openTelemetry = openTelemetry,
			intermediateDataMapperRegistry = intermediateDataMapperRegistry
		)
	}
	val nodeJson = snotyJson {
		serializersModule += nodeRegistry.serializersModule()
	}
	// TERRIBLE code only required because kotlinx.serialization has arbitrary limitations on open polymorphism
	flowRunner.json = nodeJson

	JobRunrConfigurer.configure(syncMongoClient, nodeRegistry, nodeService, flowService, nodeLogService, meterRegistry)

	val services = ServicesContainerImpl {
		register(nodeRegistry)
		register(nodeService)
		register(flowService)
		register(nodeLogService)
		register(CodecRegistry::class, codecRegistry)
	}

	KtorServer(config, buildInfo, meterRegistry, services, nodeJson)
		.start(wait = true)
}
