package me.snoty.backend.featureflags.provider

import dev.openfeature.contrib.providers.flagd.FlagdOptions
import dev.openfeature.contrib.providers.flagd.FlagdProvider
import dev.openfeature.sdk.FeatureProvider
import me.snoty.backend.config.ProviderFeatureFlagConfig
import org.koin.core.annotation.Single

@Single(binds = [OpenFeatureProvider::class])
class FlagdOpenFeatureProvider : BaseOpenFeatureProvider<ProviderFeatureFlagConfig.Flagd>(ProviderFeatureFlagConfig.Flagd::class) {
	override val name: String = "flagd"

	override fun createFeatureProvider(config: ProviderFeatureFlagConfig.Flagd): FeatureProvider {
		val flagdOptions = FlagdOptions.builder()
			.host(config.host)
			.port(config.port.toInt())
			.build()

		return FlagdProvider(flagdOptions)
	}
}
