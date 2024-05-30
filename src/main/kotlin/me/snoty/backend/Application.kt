package me.snoty.backend

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import me.snoty.backend.build.DevBuildInfo
import me.snoty.backend.config.ConfigLoaderImpl
import me.snoty.backend.database.mongo.createMongoClients
import me.snoty.backend.integration.IntegrationManager
import me.snoty.backend.integration.MongoEntityStateService
import me.snoty.backend.scheduling.JobRunrConfigurer
import me.snoty.backend.scheduling.JobRunrScheduler
import me.snoty.backend.server.KtorServer
import me.snoty.backend.spi.DevManager
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.Executors

fun main() {
	val logger = KotlinLogging.logger {}

	// ran pre-config load to allow dev functions to configure the environment
	DevManager.runDevFunctions()

	val configLoader = ConfigLoaderImpl()
	val config = configLoader.loadConfig()

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
	val dataSource = config.database.value
	val database = Database.connect(dataSource)

	val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
	val metricsPool = Executors.newScheduledThreadPool(1)
	val scheduler = JobRunrScheduler()

	val (mongoDB, syncMongoClient) = createMongoClients(config.mongodb)

	val integrationManager = IntegrationManager(scheduler) { integrationDescriptor ->
		MongoEntityStateService(mongoDB, integrationDescriptor, meterRegistry, metricsPool)
	}

	JobRunrConfigurer.configure(syncMongoClient, integrationManager, meterRegistry)
	integrationManager.startup()

	KtorServer(config, buildInfo, database, meterRegistry, integrationManager)
		.start(wait = true)
}
