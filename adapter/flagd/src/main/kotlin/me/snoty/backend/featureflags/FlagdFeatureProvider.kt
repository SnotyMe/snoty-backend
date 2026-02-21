package me.snoty.backend.featureflags

import dev.openfeature.contrib.providers.flagd.FlagdOptions
import dev.openfeature.contrib.providers.flagd.FlagdProvider
import dev.openfeature.sdk.FeatureProvider
import org.koin.core.annotation.Single

@Single
fun flagdFeatureProvider(config: FlagdFeatureFlagsConfig): FeatureProvider {
	val flagdOptions = FlagdOptions.builder()
		.host(config.host)
		.port(config.port.toInt())
		.build()

	return FlagdProvider(flagdOptions)
}
