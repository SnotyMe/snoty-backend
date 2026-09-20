package me.snoty.backend.wiring.node.metadata

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
