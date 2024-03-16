package me.snoty.backend.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.snoty.backend.config.Config
import me.snoty.backend.server.plugins.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

class KtorServer : KoinComponent {
	private val config by inject<Config>()
	private val logger = LoggerFactory.getLogger(KtorServer::class.java)

	fun start(wait: Boolean) {
		val dev = config.environment.isDev()

		if (dev) {
			logger.debug("Running in development mode")
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
		configureMonitoring()
		configureHTTP()
		configureSecurity()
		configureSerialization()
		configureDatabases(config.database.value)
		configureRouting()
	}
}
