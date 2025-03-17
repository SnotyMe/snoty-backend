package me.snoty.backend.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.sse.SSE
import me.snoty.backend.config.Config

fun Application.configureHTTP(config: Config) {
	install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
	install(XForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
	install(DefaultHeaders) {
		header("X-Engine", "Ktor")
	}
	install(CORS) {
		// hosts
		if (config.environment.isDev()) anyHost()
		config.corsHosts.forEach(::allowHost)

		// methods
		HttpMethod.DefaultMethods.forEach(::allowMethod)

		// headers
		allowHeader(HttpHeaders.Authorization)
		allowHeader(HttpHeaders.ContentType)

		// credentials
		allowCredentials = true
	}
	install(SSE)
}
