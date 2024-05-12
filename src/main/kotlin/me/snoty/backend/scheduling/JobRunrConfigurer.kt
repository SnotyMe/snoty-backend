package me.snoty.backend.scheduling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.integration.IntegrationManager
import org.jobrunr.configuration.JobRunr
import org.jobrunr.configuration.JobRunrMicroMeterIntegration
import org.jobrunr.server.JobActivator
import org.jobrunr.storage.StorageProvider
import org.jobrunr.storage.StorageProviderUtils
import org.jobrunr.storage.sql.common.DefaultSqlStorageProvider
import org.jobrunr.storage.sql.postgres.PostgresDialect
import org.jobrunr.utils.mapper.JsonMapper
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
			// `useDashboard` that binds to 0.0.0.0
			.apply {
				this@apply.javaClass.getDeclaredField("dashboardWebServer").let { webServer ->
					webServer.isAccessible = true

					fun <T> getField(name: String) = this@apply.javaClass.getDeclaredField(name).let { field ->
						field.isAccessible = true
						@Suppress("UNCHECKED_CAST")
						field.get(this@apply) as T
					}

					val jsonMapper: JsonMapper = getField("jsonMapper")
					val storageProvider: StorageProvider = getField("storageProvider")

					val jobRunrDashboardWebServerOverride = JobRunrDashboardWebServerOverride(
						storageProvider,
						jsonMapper,
						8082
					)
					webServer.set(this@apply, jobRunrDashboardWebServerOverride)
					jobRunrDashboardWebServerOverride.start()
				}
			}
			.initialize()
	}
}
