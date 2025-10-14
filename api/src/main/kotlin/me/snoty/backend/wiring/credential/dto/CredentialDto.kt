package me.snoty.backend.wiring.credential.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CredentialDto(
	val access: CredentialAccess,
	val id: String,
	val name: String,
	val data: JsonObject,
)
