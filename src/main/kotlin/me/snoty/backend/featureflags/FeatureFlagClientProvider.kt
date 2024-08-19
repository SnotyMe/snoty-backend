package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.config.ProviderFeatureFlagConfig
import me.snoty.backend.featureflags.provider.FlagdOpenFeatureProvider
import me.snoty.backend.featureflags.provider.InMemoryOpenFeatureProvider

object FeatureFlagClientProvider {
	private val logger = KotlinLogging.logger {}

	fun provideClient(featureFlagsConfig: ProviderFeatureFlagConfig): Client {
		val client = when (featureFlagsConfig) {
			is ProviderFeatureFlagConfig.Flagd -> FlagdOpenFeatureProvider.createClient(featureFlagsConfig)
			is ProviderFeatureFlagConfig.InMemory -> InMemoryOpenFeatureProvider.createClient(featureFlagsConfig)
			else -> throw IllegalArgumentException("No provider found for $featureFlagsConfig")
		}

		logger.debug { "Provided client $client for $featureFlagsConfig" }

		return client
	}
}
