package me.snoty.backend.featureflags.provider

import dev.openfeature.sdk.FeatureProvider
import dev.openfeature.sdk.providers.memory.Flag
import dev.openfeature.sdk.providers.memory.InMemoryProvider
import me.snoty.backend.config.ProviderFeatureFlagConfig

object InMemoryOpenFeatureProvider : BaseOpenFeatureProvider<ProviderFeatureFlagConfig.InMemory>() {
	override val name: String = "inmemory"

	override fun createFeatureProvider(config: ProviderFeatureFlagConfig.InMemory): FeatureProvider {
		val flags = config.flags.mapValues { (_, value) ->
			Flag.builder<Any>()
				.variant(value, value)
				.defaultVariant(value)
				.build()
		}

		return InMemoryProvider(flags)
	}
}
