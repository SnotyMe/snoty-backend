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
		val changeListeners = featureFlags.logLevelFlags.map {
			loggerFeatureFlagListener(featureFlags, it)
		}
		featureClient.on(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED) { details ->
			logger.debug { "Received change event: $details" }
			changeListeners.forEach { listener ->
				listener.configChanged()
			}
		}
	}

	private fun loggerFeatureFlagListener(featureFlags: FeatureFlags, flag: LogLevelFeatureFlag) =
		FeatureFlagChangeListener(featureFlags, flag, true) {
			val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
			flag.loggers.forEach { loggerName ->
				val logger: Logger? =
					if (loggerName == "root") loggerContext.getLogger(loggerName)
					else loggerContext.exists(loggerName)

				if (logger == null) {
					this.logger.warn { "Logger for flag ${flag.name}, package $loggerName not found" }
					return@FeatureFlagChangeListener
				}

				logger.level = Level.convertAnSLF4JLevel(it)
				this.logger.info { "Set log level for flag ${flag.name}, package $loggerName to $it" }
			}
		}

	class FeatureFlagChangeListener<T>(
		private val featureFlags: FeatureFlags,
		private val flag: FeatureFlag<T>,
		initialFire: Boolean = false,
		private val onChange: (T) -> Unit
	) {
		private var previous = featureFlags.get(flag)

		init {
			if (initialFire) {
				onChange(previous)
			}
		}

		fun configChanged() {
			val new = featureFlags.get(flag)
			if (previous != new) {
				previous = new
				onChange(new)
			}
		}
	}
}
