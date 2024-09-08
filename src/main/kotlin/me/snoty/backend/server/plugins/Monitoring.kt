package me.snoty.backend.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.ktor.v3_0.server.KtorServerTracing
import me.snoty.backend.config.Config
import org.slf4j.event.Level

fun Application.configureMonitoring(config: Config, openTelemetry: OpenTelemetry, meterRegistry: MeterRegistry) {
	install(MicrometerMetrics) {
		registry = meterRegistry
	}
	install(CallLogging) {
		level = Level.INFO
		filter { call -> call.request.path().startsWith("/") }
		callIdMdc("call-id")
	}
	install(KtorServerTracing) {
		setOpenTelemetry(openTelemetry)
	}
	install(CallId) {
		header(HttpHeaders.XRequestId)
		verify { callId: String ->
			callId.isNotEmpty()
		}
		// generate if not set already
		generate(10, dictionary = "abcdefghijklmnopqrstuvwxyz0123456789")
	}
	embeddedServer(Netty, port = config.monitoringPort.toInt()) {
		routing {
			get("/hello") {
				call.respond(HttpStatusCode.NoContent)
			}
			if (meterRegistry is PrometheusMeterRegistry) {
				get("/metrics") {
					call.respond(meterRegistry.scrape())
				}
			}
		}
	}.start(wait = false)
}
