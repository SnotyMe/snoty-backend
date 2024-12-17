package me.snoty.backend.featureflags

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import dev.openfeature.sdk.Client
import dev.openfeature.sdk.ProviderEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.LoggerFactory

object FeatureFlagsSetup {
	private val logger = KotlinLogging.logger {}

	fun setup(featureClient: Client, featureFlags: Collection<LogFeatureFlagsContainer>) {
		val changeListeners = featureFlags.flatMap { it.logLevelFeatureFlags }.map {
			loggerFeatureFlagListener(featureClient, it)
		}

		featureClient.on(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED) { details ->
			logger.debug { "Received change event: $details" }
			changeListeners.forEach { listener ->
				listener.configChanged()
			}
		}
	}

	private val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

	private fun loggerFeatureFlagListener(client: Client, flag: LogLevelFeatureFlag) =
		FeatureFlagChangeListener(client, flag, true) {
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
		private val client: Client,
		private val flag: FeatureFlag<T>,
		initialFire: Boolean = false,
		private val onChange: (T) -> Unit
	) {
		private var previous = flag.getValue(client)

		init {
			if (initialFire) {
				onChange(previous)
			}
		}

		fun configChanged() {
			val new = flag.getValue(client)
			if (previous != new) {
				previous = new
				onChange(new)
			}
		}
	}
}
