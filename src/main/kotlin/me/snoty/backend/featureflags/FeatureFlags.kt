package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import me.snoty.backend.config.Config
import org.slf4j.event.Level

@Suppress("PropertyName")
class FeatureFlags(private val config: Config, private val client: Client) {
	val logLevelFlags = mutableListOf<LogLevelFeatureFlag>()

	private fun logLevelFlag(
		name: String,
		loggerName: String,
		ifDev: Level = Level.DEBUG,
		default: Level = Level.INFO,
	): LogLevelFeatureFlag {
		val fullFlagName: String = "log_level" +
			if (name.isNotEmpty()) ".$name"
			else ""
		val flag = LogLevelFeatureFlag(
			fullFlagName,
			loggerName,
			if (config.environment.isDev()) ifDev else default
		)
		logLevelFlags.add(flag)
		return flag
	}

	val logLevel_root = logLevelFlag("", "root")
	val logLevel_jobRunr = logLevelFlag("jobrunr", "org.jobrunr")
	val logLevel_mongo = logLevelFlag("mongo", "org.mongodb.driver")
	val logLevel_mongo_commands = logLevelFlag("mongo.commands", "org.mongodb.driver.protocol.command")

	fun <T> get(flag: FeatureFlag<T>): T = flag.getValue(client)
}
