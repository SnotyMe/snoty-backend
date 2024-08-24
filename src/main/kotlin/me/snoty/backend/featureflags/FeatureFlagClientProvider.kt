package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.config.Config
import me.snoty.backend.config.ProviderFeatureFlagConfig
import me.snoty.backend.featureflags.FeatureFlagClientProvider.logger
import me.snoty.backend.featureflags.provider.FlagdOpenFeatureProvider
import me.snoty.backend.featureflags.provider.InMemoryOpenFeatureProvider
import org.koin.core.annotation.Single

object FeatureFlagClientProvider {
	internal val logger = KotlinLogging.logger {}
}

@Single
fun provideClient(config: Config): Client {
	val featureFlagsConfig = config.featureFlags.value
	val client = when (featureFlagsConfig) {
		is ProviderFeatureFlagConfig.Flagd -> FlagdOpenFeatureProvider.createClient(featureFlagsConfig)
		is ProviderFeatureFlagConfig.InMemory -> InMemoryOpenFeatureProvider.createClient(featureFlagsConfig)
		else -> throw IllegalArgumentException("No provider found for $featureFlagsConfig")
	}

	logger.debug { "Provided client $client for $featureFlagsConfig" }

	return client
}
