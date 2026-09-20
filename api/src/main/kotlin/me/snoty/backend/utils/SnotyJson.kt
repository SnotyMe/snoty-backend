package me.snoty.backend.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder

/**
 * Base JSON configuration for Snoty.
 * Does NOT support [NodeSettings][me.snoty.backend.wiring.node.NodeSettings]!
 */
val BaseSnotyJson = snotyJson {}

fun snotyJson(block: JsonBuilder.() -> Unit) = Json {
	serializersModule = kotlinxSerializersModule
	ignoreUnknownKeys = true
	encodeDefaults = true
	block()
}
