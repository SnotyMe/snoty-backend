package me.snoty.backend.scheduling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mongodb.client.MongoClient
import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.database.mongo.MONGO_DB_NAME
import me.snoty.backend.integration.IntegrationManager
import org.jobrunr.configuration.JobRunr
import org.jobrunr.configuration.JobRunrMicroMeterIntegration
import org.jobrunr.dashboard.JobRunrDashboardWebServerConfiguration
import org.jobrunr.server.JobActivator
import org.jobrunr.storage.StorageProviderUtils
import org.jobrunr.storage.nosql.mongo.MongoDBStorageProvider
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper

object JobRunrConfigurer {
	fun configure(mongoClient: MongoClient, integrationManager: IntegrationManager, meterRegistry: MeterRegistry) {
		JobRunr.configure()
			.useJsonMapper(JacksonJsonMapper(ObjectMapper().registerKotlinModule()))
			.useStorageProvider(
				MongoDBStorageProvider(
					mongoClient,
					MONGO_DB_NAME,
					"jobrunr_",
					StorageProviderUtils.DatabaseOptions.CREATE
				)
			)
			.useJobActivator(object : JobActivator {
				override fun <T : Any> activateJob(type: Class<T>): T? {
					return integrationManager.getFetchHandler(type)
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
