package me.snoty.integration.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import me.snoty.integration.common.utils.kotlinxSerializersModule

/**
 * Base JSON configuration for Snoty.
 * Does NOT support [NodeSettings][me.snoty.integration.common.wiring.node.NodeSettings]!
 */
val BaseSnotyJson = snotyJson {}

fun snotyJson(block: JsonBuilder.() -> Unit) = Json {
	serializersModule = kotlinxSerializersModule
	ignoreUnknownKeys = true
	encodeDefaults = true
	block()
}
