package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import me.snoty.backend.config.Environment
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@Single
class LogFeatureFlags(override val environment: Environment, override val client: Client) : LogFeatureFlagsContainer {
	val logLevelFlags = listOf(
		logLevelFlag("", "root"),
		logLevelFlag("http.client", "io.ktor.client"),
		logLevelFlag("http.server", "io.netty", "io.ktor.server"),
		logLevelFlag("jobrunr", "org.jobrunr"),
		logLevelFlag("mongo", "org.mongodb.driver"),
		logLevelFlag(MONGO_COMMANDS, MONGO_COMMANDS_LOGGER),
	)

	companion object {
		const val MONGO_COMMANDS = "mongo.commands"
		const val MONGO_COMMANDS_LOGGER = "org.mongodb.driver.protocol.command"
	}
}

interface LogFeatureFlagsContainer : FeatureFlagsContainer {
	val environment: Environment
}

fun LogFeatureFlagsContainer.logLevelFlag(
	name: String,
	vararg loggers: String,
	ifDev: Level = Level.DEBUG,
	default: Level = Level.INFO,
): LogLevelFeatureFlag {
	val fullFlagName: String = "log_level" +
		if (name.isNotEmpty()) ".$name"
		else ""
	return LogLevelFeatureFlag(
		name = fullFlagName,
		defaultValue = if (environment.isDev()) ifDev else default,
		loggers = loggers,
	)
}
