package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import me.snoty.backend.config.Config
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@Single
class LogFeatureFlags(private val config: Config, override val client: Client) : FeatureFlagsContainer {
	private fun logLevelFlag(
		name: String,
		vararg loggers: String,
		ifDev: Level = Level.DEBUG,
		default: Level = Level.INFO,
	): LogLevelFeatureFlag {
		val fullFlagName: String = "log_level" +
			if (name.isNotEmpty()) ".$name"
			else ""
		return LogLevelFeatureFlag(
			fullFlagName,
			if (config.environment.isDev()) ifDev else default,
			*loggers
		)
	}

	val logLevelFlags = listOf(
		logLevelFlag("", "root"),
		logLevelFlag("http.client", "io.ktor.client"),
		logLevelFlag("http.server", "io.netty", "io.ktor.server"),
		logLevelFlag("jobrunr", "org.jobrunr"),
		logLevelFlag("mongo", "org.mongodb.driver"),
		logLevelFlag("mongo.commands", "org.mongodb.driver.protocol.command"),
	)
}
