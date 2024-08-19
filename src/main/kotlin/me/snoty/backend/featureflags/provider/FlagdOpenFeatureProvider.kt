package me.snoty.backend.featureflags.provider

import dev.openfeature.contrib.providers.flagd.FlagdOptions
import dev.openfeature.contrib.providers.flagd.FlagdProvider
import dev.openfeature.sdk.FeatureProvider
import me.snoty.backend.config.ProviderFeatureFlagConfig

object FlagdOpenFeatureProvider : BaseOpenFeatureProvider<ProviderFeatureFlagConfig.Flagd>() {
	override val name: String = "flagd"

	override fun createFeatureProvider(config: ProviderFeatureFlagConfig.Flagd): FeatureProvider {
		val flagdOptions = FlagdOptions.builder()
			.host(config.host)
			.port(config.port.toInt())
			.maxEventStreamRetries(Int.MAX_VALUE)
			.build()

		return FlagdProvider(flagdOptions)
	}
}
