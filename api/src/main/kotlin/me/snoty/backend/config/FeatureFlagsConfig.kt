package me.snoty.backend.config

sealed class ProviderFeatureFlagConfig {
	data class Flagd(val host: String, val port: Short) : ProviderFeatureFlagConfig()
	data class InMemory(val flags: Map<String, String> = emptyMap()) : ProviderFeatureFlagConfig()
}

@JvmInline
value class FeatureFlagsConfig(val value: ProviderFeatureFlagConfig)
