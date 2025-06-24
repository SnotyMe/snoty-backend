package me.snoty.backend.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.core.instrument.MeterRegistry
import io.netty.handler.codec.http.HttpServerCodec
import io.opentelemetry.api.OpenTelemetry
import kotlinx.serialization.json.Json
import me.snoty.backend.config.Config
import me.snoty.backend.server.plugins.*
import me.snoty.backend.server.routing.addResources
import org.koin.core.Koin
import org.koin.core.annotation.Single
import org.koin.ktor.plugin.setKoin

@Single
class KtorServer(
	private val koin: Koin,
	private val config: Config,
	private val openTelemetry: OpenTelemetry,
	private val metricsRegistry: MeterRegistry,
	private val json: Json,
) {
	fun start(wait: Boolean) {
		embeddedServer(
			Netty,
			serverConfig {
				watchPaths = listOf("classes", "resources")
				module { installModules() }
			}
		) {
			connectors += EngineConnectorBuilder().apply {
				port = config.port.toInt()
				host = "0.0.0.0"
			}
			httpServerCodec = {
				HttpServerCodec(
					/* maxInitialLineLength = */ maxInitialLineLength,
					/* maxHeaderSize = */ config.server.maxHeaderSize,
					/* maxChunkSize = */ maxChunkSize
				)
			}
		}.start(wait = wait)
	}

	private fun Application.installModules() {
		setKoin(koin)
		configureMonitoring(config, openTelemetry, metricsRegistry)
		configureHTTP(config)
		configureSecurity(config, koin.get())
		configureSerialization(json)
		configureRouting(config)
		addResources(koin.getAll())
	}
}
