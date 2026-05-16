package me.snoty.backend.dev.authentication

data class KeycloakConfigurationResult(
	val publicClientId: String,
	val clientId: String,
	val clientSecret: String
)
