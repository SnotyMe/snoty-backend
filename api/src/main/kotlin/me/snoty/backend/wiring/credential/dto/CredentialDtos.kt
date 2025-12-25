package me.snoty.backend.wiring.credential.dto

import kotlinx.serialization.Serializable
import me.snoty.backend.authentication.Role
import me.snoty.backend.wiring.credential.Credential

@Serializable
data class PotentiallyAccessibleCredentialDto(
	val id: String,
	val scope: CredentialScope,
	val name: String,
	val requiredRole: Role?,
	/**
	 * Only set if accessible by the user
	 */
	val data: Credential?,
)

@Serializable
data class CredentialDto(
	val id: String,
	val scope: CredentialScope,
	val name: String,
	val data: Credential,
)
