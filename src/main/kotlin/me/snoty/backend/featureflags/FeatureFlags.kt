package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import me.snoty.backend.config.Config
import org.koin.core.annotation.Single
import org.slf4j.event.Level

@Suppress("PropertyName")
@Single
class FeatureFlags(private val config: Config, private val client: Client) {
	val logLevelFlags = mutableListOf<LogLevelFeatureFlag>()

	private fun logLevelFlag(
		name: String,
		vararg loggers: String,
		ifDev: Level = Level.DEBUG,
		default: Level = Level.INFO,
	): LogLevelFeatureFlag {
		val fullFlagName: String = "log_level" +
			if (name.isNotEmpty()) ".$name"
			else ""
		val flag = LogLevelFeatureFlag(
			fullFlagName,
			if (config.environment.isDev()) ifDev else default,
			*loggers
		)
		logLevelFlags.add(flag)
		return flag
	}

	val logLevel_root = logLevelFlag("", "root")
	val logLevel_http_client = logLevelFlag("http.client", "io.ktor.client")
	val logLevel_http_server = logLevelFlag("http.server", "io.netty", "io.ktor.server")
	val logLevel_jobRunr = logLevelFlag("jobrunr", "org.jobrunr")
	val logLevel_mongo = logLevelFlag("mongo", "org.mongodb.driver")
	val logLevel_mongo_commands = logLevelFlag("mongo.commands", "org.mongodb.driver.protocol.command")

	val flow_logFlow = FeatureFlagBoolean("flow.logFlow", false)
	val flow_traceConfig = FeatureFlagBoolean("flow.traceConfig", false)
	val flow_traceInput = FeatureFlagBoolean("flow.traceInput", false)

	fun <T> get(flag: FeatureFlag<T>): T = flag.getValue(client)
}
