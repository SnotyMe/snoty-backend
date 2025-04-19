package me.snoty.backend.wiring.node

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val metadataJson = Json {
	ignoreUnknownKeys = true
	encodeDefaults = true
	allowStructuredMapKeys = true
	allowSpecialFloatingPointValues = true
	coerceInputValues = true
	explicitNulls = true
	allowTrailingComma = true
	prettyPrint = true
}
