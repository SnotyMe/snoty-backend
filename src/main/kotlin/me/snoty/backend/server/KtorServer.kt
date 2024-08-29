package me.snoty.backend.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.serialization.json.Json
import me.snoty.backend.config.Config
import me.snoty.backend.server.plugins.*
import me.snoty.backend.server.routing.addResources
import org.koin.core.Koin
import org.koin.core.annotation.Single

@Single
class KtorServer(
	private val koin: Koin,
	private val config: Config,
	private val metricsRegistry: MeterRegistry,
	private val json: Json,
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
				module()
			},
			watchPaths = listOf("classes", "resources")
		).start(wait = wait)
	}

	private fun Application.module() {
		setKoin(koin)
		configureMonitoring(config, metricsRegistry)
		configureHTTP(config)
		configureSecurity(config)
		configureSerialization(json)
		configureRouting(config)
		addResources(koin.getAll())
	}
}
