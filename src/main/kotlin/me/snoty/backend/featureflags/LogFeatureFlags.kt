package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import me.snoty.backend.config.Environment
import org.koin.core.annotation.Single

@Single
class LogFeatureFlags(override val environment: Environment, override val client: Client) : LogFeatureFlagsContainer {
	override val logLevelFeatureFlags = listOf(
		logLevelFlag("", "me.snoty"),
		logLevelFlag("root", "root"),
		logLevelFlag("http.client", "io.ktor.client"),
		logLevelFlag("http.server", "io.netty", "io.ktor.server", "io.ktor.auth.jwt"),
		logLevelFlag("koin", "[Koin]"),
		logLevelFlag("jobrunr", "org.jobrunr"),
		logLevelFlag("ical4j", "net.fortuna.ical4j"),
	)
}
