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
		jobRunr
	)

	companion object {
		const val MONGO_COMMANDS = "mongo.commands"
		const val MONGO_COMMANDS_LOGGER = "org.mongodb.driver.protocol.command"
	}
}
