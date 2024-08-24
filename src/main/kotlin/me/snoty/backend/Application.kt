package me.snoty.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.opentelemetry.api.GlobalOpenTelemetry
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.serialization.modules.plus
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.config.Config
import me.snoty.backend.featureflags.FeatureFlagsSetup
import me.snoty.backend.hooks.HookRegistryImpl
import me.snoty.backend.injection.ServicesContainerImpl
import me.snoty.backend.integration.MongoEntityStateService
import me.snoty.backend.integration.flow.FlowRunnerImpl
import me.snoty.backend.integration.flow.logging.NodeLogService
import me.snoty.backend.scheduling.JobRunrConfigurer
import me.snoty.backend.scheduling.node.NodeScheduler
import me.snoty.backend.server.KtorServer
import me.snoty.backend.wiring.node.NodeHandlerContributorLookup
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.snotyJson
import me.snoty.integration.common.wiring.NodeHandlerContext
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateDataMapper
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateDataMapper
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodeRegistry
import me.snoty.integration.common.wiring.node.serializersModule
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.concurrent.Executors

class Application : KoinComponent {
	val logger = KotlinLogging.logger {}

	private val config: Config = get()
	private val buildInfo: BuildInfo = get()

	suspend fun start() {
		logger.info { "Loaded config: $config" }

		FeatureFlagsSetup.setup(get(), get())

		val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
		val openTelemetry = GlobalOpenTelemetry.get()
		val metricsPool = Executors.newScheduledThreadPool(1)

		val nodeRegistry: NodeRegistry = get()
		val nodeLogService: NodeLogService = get()

		val flowService: FlowService = get()
		val nodeService: NodeService = get()

		val intermediateDataMapperRegistry = IntermediateDataMapperRegistry()
		intermediateDataMapperRegistry[BsonIntermediateData::class] = BsonIntermediateDataMapper(get())
		intermediateDataMapperRegistry[SimpleIntermediateData::class] = SimpleIntermediateDataMapper

		val hookRegistry = HookRegistryImpl()

		NodeHandlerContributorLookup.executeContributors(nodeRegistry) { metadata ->
			val entityStateService = MongoEntityStateService(get(), metadata.descriptor, meterRegistry, metricsPool)
			entityStateService.scheduleMetricsTask()
			NodeHandlerContext(
				metadata = metadata,
				entityStateService = entityStateService,
				nodeService = nodeService,
				flowService = flowService,
				codecRegistry = get(),
				scheduler = get(),
				openTelemetry = openTelemetry,
				intermediateDataMapperRegistry = intermediateDataMapperRegistry,
				hookRegistry = hookRegistry,
				nodePersistenceServiceFactory = get()
			)
		}
		val nodeJson = snotyJson {
			serializersModule += nodeRegistry.serializersModule()
		}
		// TERRIBLE code only required because kotlinx.serialization has arbitrary limitations on open polymorphism
		get<FlowRunnerImpl>().json = nodeJson

		JobRunrConfigurer.configure(get(), nodeRegistry, nodeService, flowService, nodeLogService, meterRegistry)
		val services = ServicesContainerImpl {
			register(nodeRegistry)
			register(nodeService)
			register(flowService)
			register(nodeLogService)
			register(CodecRegistry::class, get())
		}

		newSingleThreadContext("NodeScheduler").use {
			nodeService.query(position = NodePosition.START)
				.collect(get<NodeScheduler>()::schedule)
		}

		KtorServer(config, buildInfo, meterRegistry, services, nodeJson, hookRegistry)
			.start(wait = true)
	}
}
