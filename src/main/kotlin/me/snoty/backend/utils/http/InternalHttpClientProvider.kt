package me.snoty.backend.utils.http

import io.ktor.client.*
import io.opentelemetry.api.OpenTelemetry
import me.snoty.integration.common.BaseSnotyJson
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

/**
 * Provides an opinionated Ktor [HttpClient] FOR INTERNAL USE ONLY.
 * Does NOT respect the default proxy and can therefore be used to connect directly
 * to services (e.g. Keycloak) within the network Snoty is hosted in.
 *
 * Includes out-of-the-box support for Tracing and Serialization using [BaseSnotyJson]
 */
@Single
@Named(INTERNAL_HTTP_CLIENT)
fun provideInternalHttpClient(openTelemetry: OpenTelemetry) = HttpClient {
	configureBase(openTelemetry)
}
