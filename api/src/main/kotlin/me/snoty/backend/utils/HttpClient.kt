package me.snoty.backend.utils

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor
import io.opentelemetry.instrumentation.ktor.v3_0.KtorClientTelemetry
import me.snoty.integration.common.BaseSnotyJson
import org.koin.core.annotation.Single

/**
 * Provides an opinionated Ktor [HttpClient].
 *
 * Includes out-of-the-box support for Tracing and Serialization using [BaseSnotyJson]
 *
 * Prefer to utilize this function if you need http client functionality as this will guarantee the best compatibility and observability.
 */
@Single
fun httpClient(openTelemetry: OpenTelemetry) = HttpClient {
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
