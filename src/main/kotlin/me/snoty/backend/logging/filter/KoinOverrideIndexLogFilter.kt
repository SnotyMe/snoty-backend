package me.snoty.backend.logging.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.spi.FilterReply
import dev.openfeature.sdk.Client
import me.snoty.backend.featureflags.FeatureFlagBoolean
import me.snoty.backend.featureflags.FeatureFlagsContainer
import org.koin.core.annotation.Single
import org.slf4j.Marker

@Single
class KoinOverrideIndexLogFilterFeatureFlags(override val client: Client) : FeatureFlagsContainer {
	val muteOverrideLogs by FeatureFlagBoolean("koin.muteOverrideLogs", true)
}

@Single(createdAtStart = true)
class KoinOverrideIndexLogFilter(private val featureFlags: KoinOverrideIndexLogFilterFeatureFlags) : TurboFilter() {
	override fun decide(marker: Marker?, logger: Logger?, level: Level?, format: String?, params: Array<out Any>?, t: Throwable?): FilterReply = when {
		logger?.name == "[Koin]"
			&& format?.contains("(+) override index") == true
			&& featureFlags.muteOverrideLogs
		-> FilterReply.DENY

		else -> FilterReply.NEUTRAL
	}
}
