package me.snoty.backend.config

data class Config(
	val database: DatabaseConfig,
	val port: Short = 8080,
	val publicHost: String,
	val environment: Environment,
	val authentication: OidcConfig
)
