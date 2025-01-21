package me.snoty.backend.logging.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.spi.FilterReply
import dev.openfeature.sdk.Client
import me.snoty.backend.featureflags.FeatureFlagBoolean
import me.snoty.backend.featureflags.FeatureFlagsContainer
import org.koin.core.annotation.Single
import org.koin.core.logger.KOIN_TAG
import org.slf4j.Marker

@Single
class KoinLogFilterFeatureFlags(override val client: Client) : FeatureFlagsContainer {
	// defaults to false because a log feature flag will take care of it
	val muteOverrideLogs by FeatureFlagBoolean("koin.muteOverrideLogs", false)
	// these errors are thrown and will be logged by snoty, don't need them printed twice
	val muteCreationErrorLogs by FeatureFlagBoolean("koin.muteCreationErrorLogs", true)
}

@Single(createdAtStart = true)
class KoinLogFilter(private val featureFlags: KoinLogFilterFeatureFlags) : TurboFilter() {
	override fun decide(marker: Marker?, logger: Logger?, level: Level?, format: String?, params: Array<out Any>?, t: Throwable?): FilterReply = when {
		logger?.name != KOIN_TAG -> FilterReply.NEUTRAL

		format?.contains("(+) override index") == true && featureFlags.muteOverrideLogs -> FilterReply.DENY
		format?.contains("Instance creation error") == true && featureFlags.muteCreationErrorLogs -> FilterReply.DENY

		else -> FilterReply.NEUTRAL
	}
}
