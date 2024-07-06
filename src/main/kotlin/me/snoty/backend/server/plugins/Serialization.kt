package me.snoty.backend.server.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import me.snoty.integration.common.SnotyJson

fun Application.configureSerialization() {
	install(ContentNegotiation) {
		json(SnotyJson)
	}
}
