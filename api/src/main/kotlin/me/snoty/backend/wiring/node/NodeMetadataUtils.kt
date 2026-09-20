package me.snoty.backend.wiring.node

import kotlinx.serialization.json.Json

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
