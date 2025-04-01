package me.snoty.backend.config

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.parsers.PropsParser
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*

/**
 * Optionally loads a container config for local container-based development.
 * It is a shared configuration between the application and the database container.
 * The values from this file are directly passed into the database container.
 * Thus, updating the original file will update the database container and application alike.
 */
inline fun <reified T : Any> loadContainerConfig(folder: String): Validated<ConfigFailure, T> {
	val logger = KotlinLogging.logger {}
	val configName = T::class.simpleName
	return ConfigLoaderBuilder.saneDefault()
		// don't give a shit
		.withReportPrintFn {}
		.addParser("env", PropsParser())
		// `.env.default` file - WARNING: this assumes all *.default files are .env files
		.addParser("default", PropsParser())
		// local configuration takes precedence
		.addFileSource("infra/$folder/.env", optional = true, allowEmpty = false)
		.addFileSource("infra/$folder/.env.default", optional = true, allowEmpty = false)
		.build()
		.loadConfig<T>()
		.onFailure { logger.debug { "Failed to load $configName: ${it.description()}" } }
		.map { logger.debug { "Loaded $configName: $it" }; return@map it }
}


fun ConfigLoaderBuilder.addProperties(properties: Map<String, Any>) = addPropertySource(
	PropsPropertySource(
		properties.toProperties()
	)
)

@Suppress("NOTHING_TO_INLINE")
inline fun Map<String, Any>.toProperties(): Properties =
	Properties().apply { putAll(this@toProperties) }
