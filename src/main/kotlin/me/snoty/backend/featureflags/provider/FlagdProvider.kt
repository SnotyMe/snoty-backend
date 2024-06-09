package me.snoty.backend.featureflags.provider

import dev.openfeature.contrib.providers.flagd.FlagdOptions
import dev.openfeature.contrib.providers.flagd.FlagdProvider
import dev.openfeature.sdk.Client
import dev.openfeature.sdk.OpenFeatureAPI
import me.snoty.backend.config.ProviderFeatureFlagConfig

object FlagdProvider : OpenFeatureProvider<ProviderFeatureFlagConfig.Flagd> {
	override fun createClient(config: ProviderFeatureFlagConfig.Flagd): Client {
		val flagdOptions = FlagdOptions.builder()
			.host(config.host)
			.port(config.port.toInt())
			.maxEventStreamRetries(Int.MAX_VALUE)
			.build()

		val fliptProvider = FlagdProvider(flagdOptions)
		OpenFeatureAPI.getInstance().setProviderAndWait("flagd", fliptProvider)
		return OpenFeatureAPI.getInstance().getClient("flagd")
	}
}
