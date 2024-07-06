package me.snoty.integration.common

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.opentelemetry.instrumentation.ktor.v3_0.client.KtorClientTracing

/**
 * Provides an opinionated Ktor [HttpClient].
 *
 * Includes out-of-the-box support for Tracing and Serialization using [SnotyJson]
 *
 * Prefer to utilize this function if you need http client functionality as this will guarantee the best compatibility and observability.
 * The client can be further customized in the [block].
 */
fun NodeHandlerContext.httpClient(block: HttpClientConfig<*>.() -> Unit = {}) = HttpClient {
	install(KtorClientTracing) {
		setOpenTelemetry(openTelemetry)
		setSpanNameExtractor { "HTTP ${it.method.value} ${it.url.hostWithPort}${it.url.encodedPath}" }
	}
	install(ContentNegotiation) {
		json(SnotyJson)
	}
	block()
}
