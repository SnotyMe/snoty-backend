package me.snoty.backend.config

sealed class ProviderFeatureFlagConfig {
	data class Flagd(val host: String, val port: Short) : ProviderFeatureFlagConfig()
	data object NoProvider : ProviderFeatureFlagConfig()
}

@JvmInline
value class FeatureFlagsConfig(val value: ProviderFeatureFlagConfig)
