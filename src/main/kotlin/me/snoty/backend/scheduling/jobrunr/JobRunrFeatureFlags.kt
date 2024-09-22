package me.snoty.backend.scheduling.jobrunr

import dev.openfeature.sdk.Client
import me.snoty.backend.config.Environment
import me.snoty.backend.featureflags.LogFeatureFlags.Companion.MONGO_COMMANDS
import me.snoty.backend.featureflags.LogFeatureFlagsContainer
import me.snoty.backend.featureflags.logLevelFlag
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@Single
class JobRunrFeatureFlags(override val client: Client, override val environment: Environment) : LogFeatureFlagsContainer {
	val logQueries by logLevelFlag(
		name = "$MONGO_COMMANDS.jobrunr",
		// the mongo commands logger is set to DEBUG in dev, but we want to exclude excessive JobRunr queries (see #25)
		ifDev = Level.INFO,
	)
}
