package me.snoty.backend.scheduling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mongodb.client.MongoClient
import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.database.mongo.MONGO_DB_NAME
import me.snoty.backend.scheduling.node.NodeJobHandler
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.node.NodeRegistry
import org.jobrunr.configuration.JobRunr
import org.jobrunr.configuration.JobRunrMicroMeterIntegration
import org.jobrunr.dashboard.JobRunrDashboardWebServerConfiguration
import org.jobrunr.server.JobActivator
import org.jobrunr.storage.StorageProviderUtils
import org.jobrunr.storage.nosql.mongo.MongoDBStorageProvider
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper

object JobRunrConfigurer {
	fun configure(
		mongoClient: MongoClient,
		nodeRegistry: NodeRegistry,
		nodeService: NodeService,
		flowService: FlowService,
		meterRegistry: MeterRegistry
	) {
		val jobHandler = NodeJobHandler(nodeRegistry, nodeService, flowService)
		JobRunr.configure()
			.useJsonMapper(JacksonJsonMapper(ObjectMapper().registerKotlinModule()))
			.useStorageProvider(
				MongoDBStorageProvider(
					mongoClient,
					MONGO_DB_NAME,
					"jobrunr:",
					StorageProviderUtils.DatabaseOptions.CREATE
				)
			)
			.useJobActivator(object : JobActivator {
				override fun <T : Any> activateJob(type: Class<T>): T? {
					@Suppress("UNCHECKED_CAST")
					return when (type) {
						NodeJobHandler::class.java -> jobHandler as T
						else -> null
					}
				}
			})
			.useBackgroundJobServer()
			.useMicroMeter(JobRunrMicroMeterIntegration(meterRegistry))
			.useDashboard(
				JobRunrDashboardWebServerConfiguration.usingStandardDashboardConfiguration()
					.andPort(8082)
			)
			.initialize()
	}
}
