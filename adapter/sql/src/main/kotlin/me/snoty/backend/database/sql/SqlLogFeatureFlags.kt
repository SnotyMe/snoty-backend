package me.snoty.backend.database.sql

import dev.openfeature.sdk.Client
import me.snoty.backend.config.Environment
import me.snoty.backend.featureflags.LogFeatureFlagsContainer
import me.snoty.backend.featureflags.LogLevelFeatureFlag
import me.snoty.backend.featureflags.logLevelFlag
import org.koin.core.annotation.Single

@Single
class SqlLogFeatureFlags(override val environment: Environment, override val client: Client) : LogFeatureFlagsContainer {
	override val logLevelFeatureFlags: List<LogLevelFeatureFlag> = listOf(
		logLevelFlag("exposed", "Exposed"),
		logLevelFlag("hikaricp", "com.zaxxer.hikari"),
	)
}
