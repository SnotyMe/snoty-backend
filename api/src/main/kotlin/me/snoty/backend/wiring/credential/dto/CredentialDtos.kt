package me.snoty.backend.wiring.credential.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import me.snoty.backend.authentication.Role

@Serializable
data class PotentiallyAccessibleCredentialDto(
	val scope: CredentialScope,
	val id: String,
	val name: String,
	val requiredRole: Role?,
	/**
	 * Only set if accessible by the user
	 */
	val data: JsonObject?
)

@Serializable
data class CredentialDto(
	val scope: CredentialScope,
	val id: String,
	val name: String,
	val data: JsonObject,
)
