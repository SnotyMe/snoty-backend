package me.snoty.backend.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addEnvironmentSource
import com.sksamuel.hoplite.addFileSource
import com.sksamuel.hoplite.addResourceSource
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.parsers.PropsParser
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.build.BuildInfo
import java.util.*

class ConfigLoaderImpl : ConfigLoader {
	private val logger = KotlinLogging.logger {}

	override fun loadConfig(): Config {
		val mongoContainerConfig = loadContainerConfig()

		return ConfigLoaderBuilder.default()
			.withResolveTypesCaseInsensitive()
			.addDefaultPreprocessors()
			.addEnvironmentSource(useUnderscoresAsSeparator = false)
			.addFileSource("application.local.yml", optional = true)
			.addFileSource("application.yml", optional = true)
			.addSource(PropsPropertySource(mongoContainerConfig.getOrElse { Properties() }))
			.build()
			.loadConfigOrThrow<Config>()
	}

	/**
	 * Optionally loads the MongoContainerConfig for local container-based development.
	 * It is a shared configuration between the application and the database container.
	 * The values from this file are directly passed into the database container.
	 * Thus, updating the original file will update the database container and application alike.
	 */
	private fun loadContainerConfig() = ConfigLoaderBuilder.default()
		.addParser("env", PropsParser())
		// `.env.default` file - WARNING: this assumes all *.default files are .env files
		.addParser("default", PropsParser())
		// local configuration takes precedence
		.addFileSource("infra/database/.env", optional = false, allowEmpty = false)
		.addFileSource("infra/database/.env.default", optional = true, allowEmpty = false)
		.build()
		.loadConfig<MongoContainerConfig>()
		.onFailure { logger.warn { "Failed to load MongoContainerConfig: ${it.description()}" } }
		.map {
			logger.info { "Loaded MongoContainerConfig: $it" }
			Properties().apply {
				var prefix = ""
				if (it.username != null) {
					prefix = it.username
				}
				if (it.password != null) {
					prefix += if (it.username != null) ":" else ""
					prefix += it.password.value
				}
				if (prefix.isNotEmpty()) {
					prefix += "@"
				}
				setProperty("mongodb.connectionString",
					"mongodb://${prefix}localhost:${it.port}/"
				)
			}
		}

	override fun loadBuildInfo(): BuildInfo = ConfigLoaderBuilder.default()
		.addResourceSource("/buildinfo.properties")
		.build()
		.loadConfigOrThrow<BuildInfo>()
}
