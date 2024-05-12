package me.snoty.backend.scheduling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.integration.IntegrationManager
import org.jobrunr.configuration.JobRunr
import org.jobrunr.configuration.JobRunrMicroMeterIntegration
import org.jobrunr.dashboard.JobRunrDashboardWebServerConfiguration
import org.jobrunr.server.JobActivator
import org.jobrunr.storage.StorageProviderUtils
import org.jobrunr.storage.sql.common.DefaultSqlStorageProvider
import org.jobrunr.storage.sql.postgres.PostgresDialect
import org.jobrunr.utils.mapper.jackson.JacksonJsonMapper
import javax.sql.DataSource

object JobRunrConfigurer {
	fun configure(dataSource: DataSource, integrationManager: IntegrationManager, meterRegistry: MeterRegistry) {
		JobRunr.configure()
			.useJsonMapper(JacksonJsonMapper(ObjectMapper().registerKotlinModule()))
			.useStorageProvider(
				DefaultSqlStorageProvider(
					dataSource,
					PostgresDialect(),
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
