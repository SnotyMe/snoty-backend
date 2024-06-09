package me.snoty.backend.featureflags

import dev.openfeature.sdk.Client
import me.snoty.backend.config.Config
import org.slf4j.event.Level

class FeatureFlags(config: Config, private val client: Client) {
	val logLevel = EnumFeatureFlag(
		"log_level",
		if (config.environment.isDev()) Level.DEBUG else Level.INFO,
		Level::class
	)

	fun <T> get(flag: FeatureFlag<T>): T = flag.getValue(client)
}
