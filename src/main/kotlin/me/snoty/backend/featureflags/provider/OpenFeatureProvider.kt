package me.snoty.backend.featureflags.provider

import dev.openfeature.sdk.Client
import me.snoty.backend.config.ProviderFeatureFlagConfig

interface OpenFeatureProvider<C : ProviderFeatureFlagConfig> {
	fun createClient(config: C): Client
}
