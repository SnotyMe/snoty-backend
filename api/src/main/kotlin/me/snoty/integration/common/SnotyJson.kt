package me.snoty.integration.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import me.snoty.backend.utils.UUIDSerializer
import me.snoty.integration.common.utils.kotlinxSerializersModule
import java.util.*

/**
 * Base JSON configuration for Snoty.
 * Does NOT support [NodeSettings][me.snoty.integration.common.wiring.node.NodeSettings]!
 */
val BaseSnotyJson = snotyJson {}

fun snotyJson(block: JsonBuilder.() -> Unit) = Json {
	serializersModule = kotlinxSerializersModule + SerializersModule {
		contextual(UUID::class, UUIDSerializer)
	}
	ignoreUnknownKeys = true
	encodeDefaults = true
	block()
}
