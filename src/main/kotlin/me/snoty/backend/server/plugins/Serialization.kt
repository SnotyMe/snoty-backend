package me.snoty.backend.server.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import me.snoty.backend.utils.UUIDSerializer
import me.snoty.integration.common.utils.kotlinxSerializersModule
import java.util.*

val ktorJson = Json {
	serializersModule = kotlinxSerializersModule + SerializersModule {
		contextual(UUID::class, UUIDSerializer)
	}
	ignoreUnknownKeys = true
}

fun Application.configureSerialization() {
	install(ContentNegotiation) {
		json(ktorJson)
	}
}
