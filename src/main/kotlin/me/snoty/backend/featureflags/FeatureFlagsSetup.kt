package me.snoty.backend.featureflags

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import dev.openfeature.sdk.Client
import dev.openfeature.sdk.ProviderEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory

object FeatureFlagsSetup {
	val logger = KotlinLogging.logger {}

	fun setup(featureClient: Client, featureFlags: FeatureFlags) {
		val changeListeners = listOf(
			FeatureFlagChangeListener(featureFlags, featureFlags.logLevel) {
				logger.info { "Log Level changed to $it" }
				val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
				val logger: Logger? = loggerContext.getLogger("root")
				logger?.level = Level.convertAnSLF4JLevel(it)
			}
		)
		featureClient.on(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED) { details ->
			logger.debug { "Received change event: $details" }
			changeListeners.forEach { listener ->
				listener.configChanged()
			}
		}
	}

	class FeatureFlagChangeListener<T>(private val featureFlags: FeatureFlags, private val flag: FeatureFlag<T>, private val onChange: (T) -> Unit) {
		private var previous = featureFlags.get(flag)

		fun configChanged() {
			val new = featureFlags.get(flag)
			if (previous != new) {
				previous = new
				onChange(new)
			}
		}
	}
}
