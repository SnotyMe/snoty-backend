package me.snoty.backend.server.plugins

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.ktor.v3_0.server.KtorServerTracing
import me.snoty.backend.config.Config
import org.koin.core.annotation.Single
import org.slf4j.event.Level
import ch.qos.logback.classic.Level as LogbackLevel

fun Application.configureMonitoring(config: Config, openTelemetry: OpenTelemetry, meterRegistry: MeterRegistry) {
	install(MicrometerMetrics) {
		registry = meterRegistry
		// will be built-in once https://github.com/ktorio/ktor/pull/4579 is merged
		meterBinders += UptimeMetrics()
	}
	install(CallLogging) {
		level = Level.INFO
		// doesn't play nicely with OpenTelemetry and the custom color log format in dev
		disableDefaultColors()
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

@Single
class MonitoringServerFilter(config: Config) : Filter<ILoggingEvent>() {
	private val isDev = config.environment.isDev()

	override fun decide(event: ILoggingEvent): FilterReply {
		if (!isDev) {
			return FilterReply.NEUTRAL
		}

		return when {
			LogbackLevel.TRACE == event.level && event.formattedMessage.contains("Trace for [metrics]") -> FilterReply.DENY
			else -> FilterReply.NEUTRAL
		}
	}
}
