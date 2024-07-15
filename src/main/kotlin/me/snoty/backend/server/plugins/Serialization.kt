package me.snoty.backend.server.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization(json: Json) {
	install(ContentNegotiation) {
		json(json)
	}
}
