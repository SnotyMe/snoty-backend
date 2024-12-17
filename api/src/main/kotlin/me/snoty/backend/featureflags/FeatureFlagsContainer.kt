package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import me.snoty.backend.config.Environment
import org.slf4j.event.Level

interface FeatureFlagsContainer {
	val client: Client

	operator fun <T> FeatureFlag<T>.getValue(thisRef: FeatureFlagsContainer, property: Any?): T = getValue(thisRef.client)

	fun <T> getValue(featureFlag: FeatureFlag<T>): T = featureFlag.getValue(client)
}

interface LogFeatureFlagsContainer : FeatureFlagsContainer {
	val environment: Environment

	val logLevelFeatureFlags: List<LogLevelFeatureFlag>
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
