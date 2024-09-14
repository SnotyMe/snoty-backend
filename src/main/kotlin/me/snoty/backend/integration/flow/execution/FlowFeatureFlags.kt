package me.snoty.backend.integration.flow.execution

import dev.openfeature.sdk.Client
import me.snoty.backend.featureflags.FeatureFlagBoolean
import me.snoty.backend.featureflags.FeatureFlagLong
import me.snoty.backend.featureflags.FeatureFlagsContainer
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.days

@Single
class FlowFeatureFlags(override val client: Client) : FeatureFlagsContainer {
	val logFlow by FeatureFlagBoolean("flow.logFlow", false)
	val traceConfig by FeatureFlagBoolean("flow.traceConfig", false)
	val traceInput by FeatureFlagBoolean("flow.traceInput", false)
	val expirationSeconds by FeatureFlagLong("flow.expirationSeconds", 7.days.inWholeSeconds)
}
