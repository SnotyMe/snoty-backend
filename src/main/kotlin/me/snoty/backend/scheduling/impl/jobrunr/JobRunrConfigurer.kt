package me.snoty.backend.scheduling.impl.jobrunr

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mongodb.client.MongoClient
import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.database.mongo.MONGO_DB_NAME
import me.snoty.backend.scheduling.impl.jobrunr.node.JobRunrNodeJobHandler
import org.jobrunr.configuration.JobRunr
import org.jobrunr.configuration.JobRunrMicroMeterIntegration
import org.jobrunr.dashboard.JobRunrDashboardWebServerConfiguration
import org.jobrunr.server.JobActivator
import org.jobrunr.storage.StorageProviderUtils
import org.jobrunr.storage.nosql.mongo.MongoDBStorageProvider
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper
import org.koin.core.annotation.Single

@Single
class JobRunrConfigurer(
	private val mongoClient: MongoClient,
	private val meterRegistry: MeterRegistry,
	private val jobHandler: JobRunrNodeJobHandler,
) {
	fun configure() {
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
						JobRunrNodeJobHandler::class.java -> jobHandler as T
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
