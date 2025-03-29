package me.snoty.backend.featureflags.provider

import dev.openfeature.contrib.providers.flagd.FlagdOptions
import dev.openfeature.contrib.providers.flagd.FlagdProvider
import dev.openfeature.sdk.FeatureProvider
import io.opentelemetry.api.OpenTelemetry
import me.snoty.backend.config.ProviderFeatureFlagConfig
import org.koin.core.annotation.Single

@Single(binds = [OpenFeatureProvider::class])
class FlagdOpenFeatureProvider(private val openTelemetry: OpenTelemetry) : BaseOpenFeatureProvider<ProviderFeatureFlagConfig.Flagd>(ProviderFeatureFlagConfig.Flagd::class) {
	override val name: String = "flagd"

	override fun createFeatureProvider(config: ProviderFeatureFlagConfig.Flagd): FeatureProvider {
		val flagdOptions = FlagdOptions.builder()
			.host(config.host)
			.port(config.port.toInt())
			.openTelemetry(openTelemetry)
			.build()

		return FlagdProvider(flagdOptions)
	}
}
