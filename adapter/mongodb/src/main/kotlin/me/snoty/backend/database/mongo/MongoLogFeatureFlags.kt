package me.snoty.backend.database.mongo

import dev.openfeature.sdk.Client
import me.snoty.backend.config.Environment
import me.snoty.backend.featureflags.LogFeatureFlagsContainer
import me.snoty.backend.featureflags.LogLevelFeatureFlag
import me.snoty.backend.featureflags.logLevelFlag
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@Single
class MongoLogFeatureFlags(override val environment: Environment, override val client: Client) : LogFeatureFlagsContainer {
	val jobRunr = logLevelFlag(
		name = "$MONGO_COMMANDS.jobrunr",
		// the mongo commands logger is set to DEBUG in dev, but we want to exclude excessive JobRunr queries (see #25)
		ifDev = Level.INFO,
	)
	override val logLevelFeatureFlags: List<LogLevelFeatureFlag> = listOf(
		logLevelFlag(MONGO_COMMANDS, MONGO_COMMANDS_LOGGER),
		// excessive logging, therefore set to INFO in all environments, can be changed to DEBUG if needed
		logLevelFlag("$MONGO_FLAG_PREFIX.tracing", "me.snoty.backend.database.mongo.tracing", ifDev = Level.INFO, default = Level.INFO),
		jobRunr
	)

	companion object {
		const val MONGO_FLAG_PREFIX = "mongo"
		const val MONGO_COMMANDS = "$MONGO_FLAG_PREFIX.commands"
		const val MONGO_COMMANDS_LOGGER = "org.mongodb.driver.protocol.command"
	}
}
