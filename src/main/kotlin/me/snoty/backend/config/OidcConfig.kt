package me.snoty.backend.config

data class OidcConfig(
	val serverUrl: String,
	val issuerUrl: String = serverUrl,
	val oidcUrl: String = "$serverUrl/protocol/openid-connect",
	val clientId: String,
	val clientSecret: String
)
