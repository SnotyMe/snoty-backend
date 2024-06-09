package me.snoty.backend.server.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import me.snoty.backend.utils.UUIDSerializer
import me.snoty.backend.utils.httpStatusExceptionModule
import me.snoty.integration.common.utils.kotlinxSerializersModule
import java.util.*

fun Application.configureSerialization() {
	install(ContentNegotiation) {
		json(Json {
			serializersModule = httpStatusExceptionModule + kotlinxSerializersModule + SerializersModule {
				contextual(UUID::class, UUIDSerializer)
			}
			ignoreUnknownKeys = true
		})
	}
}
