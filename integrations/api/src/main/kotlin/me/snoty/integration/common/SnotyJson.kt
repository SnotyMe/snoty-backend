package me.snoty.integration.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import me.snoty.backend.utils.UUIDSerializer
import me.snoty.integration.common.utils.kotlinxSerializersModule
import java.util.*

val SnotyJson = Json {
	serializersModule = kotlinxSerializersModule + SerializersModule {
		contextual(UUID::class, UUIDSerializer)
	}
	ignoreUnknownKeys = true
}
