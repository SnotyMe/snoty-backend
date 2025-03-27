package me.snoty.backend.observability

import dev.openfeature.sdk.Client
import me.snoty.backend.featureflags.FeatureFlagBoolean
import me.snoty.backend.featureflags.FeatureFlagsContainer
import org.koin.core.annotation.Single

@Single
class OpenTelemetryFeatureFlags(override val client: Client) : FeatureFlagsContainer {
	// https://github.com/SnotyMe/snoty-backend/issues/169
	val muteJobRunrQueries by FeatureFlagBoolean("otel.muteJobRunrQueries", true)
}