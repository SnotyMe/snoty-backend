package me.snoty.backend.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.core.instrument.MeterRegistry
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.config.Config
import me.snoty.backend.integration.IntegrationManager
import me.snoty.backend.server.plugins.*

class KtorServer(
	private val config: Config,
	private val buildInfo: BuildInfo,
	private val metricsRegistry: MeterRegistry,
	private val integrationManager: IntegrationManager
) {
	private val logger = KotlinLogging.logger {}

	fun start(wait: Boolean) {
		val dev = config.environment.isDev()

		if (dev) {
			logger.debug { "Running in development mode" }
			System.setProperty("io.ktor.development", "true")
		}

		embeddedServer(
			Netty,
			port = config.port.toInt(),
			host = "0.0.0.0",
			module = { module() },
			watchPaths = listOf("classes", "resources")
		).start(wait = wait)
	}

	private fun Application.module() {
		configureMonitoring(config, metricsRegistry)
		configureHTTP()
		configureSecurity(config)
		configureSerialization()
		configureRouting(config)
		addResources(buildInfo, integrationManager)
	}
}
