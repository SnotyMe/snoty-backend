package me.snoty.backend.config

data class Config(
	val server: ServerConfig = ServerConfig(),
	val port: Short = 8080,
	val monitoringPort: Short = 9000,
	val publicHost: String,
	val corsHosts: List<String> = emptyList(),
	val environment: Environment,
	val featureFlags: FeatureFlagsConfig,
)
