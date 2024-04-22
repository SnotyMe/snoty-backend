package me.snoty.backend.server.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import me.snoty.backend.server.handler.httpStatusExceptionModule

fun Application.configureSerialization() {
	install(ContentNegotiation) {
		json(Json {
			serializersModule = httpStatusExceptionModule
			ignoreUnknownKeys = true
		})
	}
}
