package me.snoty.backend.wiring.credential.dto

import kotlinx.serialization.Serializable

@Serializable
data class EnumeratedCredentialDto(
	val id: String,
	val name: String,
	val access: CredentialAccess,
)
