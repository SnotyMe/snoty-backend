package me.snoty.backend.utils

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.ktor.v3_0.client.KtorClientTracing
import me.snoty.integration.common.BaseSnotyJson
import org.koin.core.annotation.Named
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Provides an opinionated Ktor [HttpClient].
 *
 * Includes out-of-the-box support for Tracing and Serialization using [BaseSnotyJson]
 *
 * Prefer to utilize this function if you need http client functionality as this will guarantee the best compatibility and observability.
 */
@Single
fun provideHttpClient(@Provided openTelemetry: OpenTelemetry) = HttpClient {
	install(KtorClientTracing) {
		setOpenTelemetry(openTelemetry)
		setSpanNameExtractor { "HTTP ${it.method.value} ${it.url.hostWithPort}${it.url.encodedPath}" }
	}
	install(ContentNegotiation) {
		json(BaseSnotyJson)
	}
	// IMPORTANT: this may be changed by the user if failures are expected and handled explicitly
	expectSuccess = true
}

@Named("publicHttpClient")
annotation class PublicHttpClient

/**
 * Provides a public Ktor [HttpClient].
 * Same as [provideHttpClient] but prevents fetches to internal services using private IPs.
 */
@Single(binds = [])
@PublicHttpClient
fun providePublicHttpClient(httpClient: HttpClient) = httpClient.config {
	// IMPORTANT: this may be changed by the user if internal services are to be accessed
	engine {
		this as? ApacheEngineConfig ?: return@engine
		this.followRedirects
	}
}
