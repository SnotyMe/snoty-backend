package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import me.snoty.backend.config.Config
import org.koin.core.annotation.Single
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.days

@Suppress("PropertyName")
@Single
class FeatureFlags(private val config: Config, override val client: Client) : FeatureFlagsContainer {
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

	val logLevel_root by logLevelFlag("", "root")
	val logLevel_http_client by logLevelFlag("http.client", "io.ktor.client")
	val logLevel_http_server by logLevelFlag("http.server", "io.netty", "io.ktor.server")
	val logLevel_jobRunr by logLevelFlag("jobrunr", "org.jobrunr")
	val logLevel_mongo by logLevelFlag("mongo", "org.mongodb.driver")
	val logLevel_mongo_commands by logLevelFlag("mongo.commands", "org.mongodb.driver.protocol.command")

	val flow_logFlow by FeatureFlagBoolean("flow.logFlow", false)
	val flow_traceConfig by FeatureFlagBoolean("flow.traceConfig", false)
	val flow_traceInput by FeatureFlagBoolean("flow.traceInput", false)

	val flow_expirationSeconds by FeatureFlagLong("flow.expirationSeconds", 7.days.inWholeSeconds)

	fun <T> get(flag: FeatureFlag<T>): T = flag.getValue(client)
}
