package me.snoty.backend.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*
import me.snoty.backend.config.Environment

fun Application.configureHTTP(environment: Environment) {
	install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
	install(XForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
	install(DefaultHeaders) {
		header("X-Engine", "Ktor")
	}
	install(CORS) {
		// TODO: configure allowed hosts in prod
		if (environment.isDev()) anyHost()
		HttpMethod.DefaultMethods.forEach(::allowMethod)
		allowHeader(HttpHeaders.Authorization)
		allowHeader(HttpHeaders.ContentType)
	}
}
