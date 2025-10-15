package me.snoty.backend.server.resources.wiring.credential

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CredentialCreateDto(
	val type: String,
	val name: String,
	val data: JsonElement,
	// TODO: CredentialAccess
)

@Serializable
data class CredentialUpdateDto(
	val name: String,
	val data: JsonElement,
)
