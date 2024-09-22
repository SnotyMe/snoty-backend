package me.snoty.backend.scheduling.jobrunr

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mongodb.client.MongoClient
import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.database.mongo.MONGO_DB_NAME
import org.jobrunr.configuration.JobRunr
import org.jobrunr.configuration.JobRunrMicroMeterIntegration
import org.jobrunr.dashboard.JobRunrDashboardWebServerConfiguration
import org.jobrunr.server.JobActivator
import org.jobrunr.storage.StorageProvider
import org.jobrunr.storage.StorageProviderUtils
import org.jobrunr.storage.nosql.mongo.MongoDBStorageProvider
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper
import org.koin.core.Koin
import org.koin.core.annotation.Single

@Single
class JobRunrConfigurer(
	private val meterRegistry: MeterRegistry,
	private val storageProvider: StorageProvider,
	private val koin: Koin,
) {
	fun configure() {
		JobRunr.configure()
			.useJsonMapper(JacksonJsonMapper(ObjectMapper().registerKotlinModule()))
			.useStorageProvider(storageProvider)
			.useJobActivator(object : JobActivator {
				override fun <T : Any> activateJob(type: Class<T>): T? = koin.getOrNull(type.kotlin)
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

@Single
fun storageProvider(mongoClient: MongoClient): StorageProvider = MongoDBStorageProvider(
	/* mongoClient = */ mongoClient,
	/* dbName = */ MONGO_DB_NAME,
	/* collectionPrefix = */ JOBRUNR_COLLECTION_PREFIX,
	/* databaseOptions = */ StorageProviderUtils.DatabaseOptions.CREATE
)

const val JOBRUNR_COLLECTION_PREFIX = "jobrunr:"
