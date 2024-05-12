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
		val pgContainerConfig = loadContainerConfig()

		return ConfigLoaderBuilder.default()
			.withResolveTypesCaseInsensitive()
			.addDefaultPreprocessors()
			.addEnvironmentSource(useUnderscoresAsSeparator = false)
			.addFileSource("application.local.yml", optional = true)
			.addFileSource("application.yml", optional = true)
			.addSource(PropsPropertySource(pgContainerConfig.getOrElse { Properties() }))
			.build()
			.loadConfigOrThrow<Config>()
	}

	/**
	 * Optionally loads the PGContainerConfig for local container-based development.
	 * It is a shared configuration between the application and the database container.
	 * The values from this file are directly passed into the database container.
	 * Thus, updating the original file will update the database container and application alike.
	 */
	private fun loadContainerConfig() = ConfigLoaderBuilder.default()
		.addParser("env", PropsParser())
		// `.env.default` file - WARNING: this assumes all *.default files are .env files
		.addParser("default", PropsParser())
		// local configuration takes precedence
		.addFileSource("infra/database/.env.default", optional = true, allowEmpty = false)
		.addFileSource("infra/database/.env", optional = false, allowEmpty = false)
		.build()
		.loadConfig<PGContainerConfig>()
		.onFailure { logger.warn { "Failed to load PGContainerConfig: ${it.description()}" } }
		.map {
			logger.info { "Loaded PGContainerConfig: $it" }
			Properties().apply {
				setProperty("database.username", it.user)
				setProperty("database.password", it.password.value)
				setProperty("database.jdbcUrl", "jdbc:postgresql://localhost:${it.port}/${it.db}")
			}
		}

	override fun loadBuildInfo(): BuildInfo = ConfigLoaderBuilder.default()
		.addResourceSource("/buildinfo.properties")
		.build()
		.loadConfigOrThrow<BuildInfo>()
}
