package me.snoty.backend.scheduling.jobrunr

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import org.jobrunr.configuration.JobRunr
import org.jobrunr.configuration.JobRunrMicroMeterIntegration
import org.jobrunr.dashboard.JobRunrDashboardWebServerConfiguration
import org.jobrunr.kotlin.utils.mapper.KotlinxSerializationJsonMapper
import org.jobrunr.server.JobActivator
import org.jobrunr.storage.StorageProvider
import org.koin.core.Koin
import org.koin.core.annotation.Single

@Single
class JobRunrConfigurer(
	private val reconnecter: JobRunrReconnecter,
	private val meterRegistry: MeterRegistry,
	private val storageProvider: StorageProvider,
	private val koin: Koin,
) {
	@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
	fun initialize() = JobRunr.configure()
		.useJsonMapper(KotlinxSerializationJsonMapper())
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
		.initialize()!!
		.also {
			reconnecter.startReconnectLoop()
		}
}
