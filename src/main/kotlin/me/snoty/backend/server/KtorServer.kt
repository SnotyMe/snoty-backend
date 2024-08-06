package me.snoty.backend.server

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.serialization.json.Json
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.config.Config
import me.snoty.backend.featureflags.FeatureFlags
import me.snoty.backend.injection.ServicesContainer
import me.snoty.backend.server.plugins.*
import org.slf4j.LoggerFactory

class KtorServer(
	private val config: Config,
	private val featureFlags: FeatureFlags,
	private val buildInfo: BuildInfo,
	private val metricsRegistry: MeterRegistry,
	private val servicesContainer: ServicesContainer,
	private val json: Json
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
			module = {
				val loggerFactory = LoggerFactory.getILoggerFactory() as LoggerContext
				val ktorRootLogger = loggerFactory.exists("io.ktor.server")
				// workaround for https://youtrack.jetbrains.com/issue/KTOR-7193/Tracing-allow-changing-log-level-at-runtime
				ktorRootLogger.level = Level.TRACE
				module()
				ktorRootLogger.level = Level.convertAnSLF4JLevel(featureFlags.get(featureFlags.logLevel_http_server))
			},
			watchPaths = listOf("classes", "resources")
		).start(wait = wait)
	}

	private fun Application.module() {
		configureMonitoring(config, metricsRegistry)
		configureHTTP(config)
		configureSecurity(config)
		configureSerialization(json)
		configureRouting(config)
		addResources(buildInfo, servicesContainer, json)
	}
}
