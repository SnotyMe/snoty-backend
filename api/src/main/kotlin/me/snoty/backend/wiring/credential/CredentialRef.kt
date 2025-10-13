package me.snoty.backend.wiring.credential

import kotlinx.serialization.Serializable

@Serializable
data class CredentialRef<T : Credential>(
	val credentialId: String? = null,
)
