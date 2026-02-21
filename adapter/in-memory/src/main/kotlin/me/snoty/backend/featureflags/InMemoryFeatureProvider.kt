package me.snoty.backend.featureflags

import dev.openfeature.sdk.FeatureProvider
import dev.openfeature.sdk.providers.memory.Flag
import dev.openfeature.sdk.providers.memory.InMemoryProvider
import org.koin.core.annotation.Single

@Single
fun inMemoryFeatureProvider(config: InMemoryFeatureFlagsConfig): FeatureProvider {
	val flags = config.flags.mapValues { (_, value) ->
		Flag.builder<Any>()
			.variant(value, value)
			.defaultVariant(value)
			.build()
	}

	return InMemoryProvider(flags)
}
