package me.snoty.backend.utils.http

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor
import io.opentelemetry.instrumentation.ktor.v3_0.KtorClientTelemetry
import me.snoty.integration.common.BaseSnotyJson
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger {}

/**
 * Provides an opinionated Ktor [HttpClient].
 *
 * Includes out-of-the-box support for Tracing and Serialization using [BaseSnotyJson]
 *
 * Prefer to utilize this function if you need http client functionality as this will guarantee the best compatibility and observability.
 */
@Single
fun httpClient(openTelemetry: OpenTelemetry, proxyConfigWrapper: ProxyConfigWrapper) = HttpClient(OkHttp) {
	configureBase(openTelemetry)
	proxyConfigWrapper.defaultProxy?.let {
		logger.info { "Configuring default HTTP Client with proxy $it" }
		configureProxy(it)
	}
}

fun HttpClientConfig<*>.configureBase(openTelemetry: OpenTelemetry) {
	install(KtorClientTelemetry) {
		setOpenTelemetry(openTelemetry)
		spanNameExtractor {
			SpanNameExtractor {
				"${it.method.value} ${it.url.hostWithPort}${it.url.encodedPath}"
			}
		}
	}
	install(ContentNegotiation) {
		json(BaseSnotyJson)
	}
	// IMPORTANT: this may be changed by the user if failures are expected and handled explicitly
	expectSuccess = true
}
