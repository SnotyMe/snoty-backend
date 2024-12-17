package me.snoty.backend.database.mongo.tracing

import dev.openfeature.sdk.Client
import me.snoty.backend.featureflags.FeatureFlagBoolean
import me.snoty.backend.featureflags.FeatureFlagsContainer
import org.koin.core.annotation.Single

@Single
class MongoTracingFeatureFlags(override val client: Client) : FeatureFlagsContainer {
	val traceQueries by FeatureFlagBoolean("mongo.traceQueries", false)
}
