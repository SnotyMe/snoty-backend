package me.snoty.backend.config

data class Config(
	val mongodb: MongoConfig,
	val port: Short = 8080,
	val monitoringPort: Short = 9000,
	val publicHost: String,
	val corsHosts: List<String> = emptyList(),
	val environment: Environment,
	val authentication: OidcConfig,
	val featureFlags: FeatureFlagsConfig
)
