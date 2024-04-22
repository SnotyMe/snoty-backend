package me.snoty.backend.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.event.Level

fun Application.configureMonitoring(meterRegistry: MeterRegistry) {
	install(MicrometerMetrics) {
		registry = meterRegistry
	}
	install(CallLogging) {
		level = Level.INFO
		filter { call -> call.request.path().startsWith("/") }
		callIdMdc("call-id")
	}
	install(CallId) {
		header(HttpHeaders.XRequestId)
		verify { callId: String ->
			callId.isNotEmpty()
		}
		// generate if not set already
		generate(10)
	}
	if (meterRegistry is PrometheusMeterRegistry) {
		routing {
			get("/metrics") {
				call.respond(meterRegistry.scrape())
			}
		}
	}
}
