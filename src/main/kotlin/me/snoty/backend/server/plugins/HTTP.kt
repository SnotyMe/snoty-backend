package me.snoty.backend.server.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.forwardedheaders.*

fun Application.configureHTTP() {
	install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
	install(XForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
	install(DefaultHeaders) {
		header("X-Engine", "Ktor")
	}
}
