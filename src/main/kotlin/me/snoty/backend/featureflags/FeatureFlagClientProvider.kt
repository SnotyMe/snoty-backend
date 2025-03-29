package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.config.Config
import me.snoty.backend.config.ProviderFeatureFlagConfig
import me.snoty.backend.featureflags.FeatureFlagClientProvider.logger
import me.snoty.backend.featureflags.provider.OpenFeatureProvider
import org.koin.core.annotation.Single

object FeatureFlagClientProvider {
	internal val logger = KotlinLogging.logger {}
}

@Single
fun provideClient(config: Config, openFeatureProviders: List<OpenFeatureProvider<ProviderFeatureFlagConfig>>): Client {
	val featureFlagsConfig = config.featureFlags.value
	
	for (provider in openFeatureProviders) {
		if (provider.configClass.isInstance(featureFlagsConfig)) {
			val client = provider.createClient(featureFlagsConfig)
			logger.debug { "Provided client $client for $featureFlagsConfig" }
			return client
		}
	}

	throw IllegalStateException("No feature flag provider found for config $featureFlagsConfig")
}
