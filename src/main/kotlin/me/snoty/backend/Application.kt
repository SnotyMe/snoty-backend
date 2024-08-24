package me.snoty.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.serialization.modules.plus
import me.snoty.backend.config.Config
import me.snoty.backend.featureflags.FeatureFlagsSetup
import me.snoty.backend.integration.NodeHandlerContributorLookup
import me.snoty.backend.integration.flow.FlowRunnerImpl
import me.snoty.backend.integration.flow.logging.NodeLogService
import me.snoty.backend.scheduling.JobRunrConfigurer
import me.snoty.backend.scheduling.node.NodeScheduler
import me.snoty.backend.server.KtorServer
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.model.NodePosition
import me.snoty.integration.common.snotyJson
import me.snoty.integration.common.wiring.data.IntermediateDataMapperRegistry
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateData
import me.snoty.integration.common.wiring.data.impl.BsonIntermediateDataMapper
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateData
import me.snoty.integration.common.wiring.data.impl.SimpleIntermediateDataMapper
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.koin.core.Koin

class Application(val koin: Koin) {
	val logger = KotlinLogging.logger {}

	inline fun <reified T : Any> get() = koin.get<T>()

	private val config: Config = get()

	suspend fun start() {
		FeatureFlagsSetup.setup(get(), get())

		val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

		val nodeRegistry: NodeRegistry = get()
		val nodeLogService: NodeLogService = get()

		val flowService: FlowService = get()
		val nodeService: NodeService = get()

		val intermediateDataMapperRegistry: IntermediateDataMapperRegistry = get()
		intermediateDataMapperRegistry[BsonIntermediateData::class] = BsonIntermediateDataMapper(get())
		intermediateDataMapperRegistry[SimpleIntermediateData::class] = SimpleIntermediateDataMapper

		NodeHandlerContributorLookup(koin).executeContributors()
/*
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
*/

		JobRunrConfigurer.configure(get(), nodeRegistry, nodeService, flowService, nodeLogService, meterRegistry)

		newSingleThreadContext("NodeScheduler").use {
			nodeService.query(position = NodePosition.START)
				.collect(get<NodeScheduler>()::schedule)
		}

		get<KtorServer>().start(wait = true)
	}
}
