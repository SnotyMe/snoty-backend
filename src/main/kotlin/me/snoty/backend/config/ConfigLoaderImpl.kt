package me.snoty.backend.config

import com.sksamuel.hoplite.*
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.parsers.PropsParser
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.build.BuildInfo
import java.util.*

class ConfigLoaderImpl : ConfigLoader {
	private val logger = KotlinLogging.logger {}

	@OptIn(ExperimentalHoplite::class)
	override fun loadConfig(): Config {
		val mongoContainerConfig = loadContainerConfig<MongoContainerConfig>("database").map {
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

		val flagdContainerConfig = loadContainerConfig<FlagdContainerConfig>("featureflags").map {
			Properties().apply {
				val prefix = Config::featureFlags.name + "."
				setProperty(prefix + "type", ProviderFeatureFlagConfig.Flagd::class.simpleName)
				setProperty(prefix + ProviderFeatureFlagConfig.Flagd::host.name, "localhost")
				setProperty(prefix + ProviderFeatureFlagConfig.Flagd::port.name, it.port.toString())
			}
		}

		return ConfigLoaderBuilder.default()
			// don't give a shit
			.withReportPrintFn {}
			.withResolveTypesCaseInsensitive()
			.withExplicitSealedTypes("type")
			.addDefaultPreprocessors()
			.addEnvironmentSource(useUnderscoresAsSeparator = false)
			.addFileSource("application.local.yml", optional = true)
			.addFileSource("application.yml", optional = true)
			.addSource(PropsPropertySource(mongoContainerConfig.getOrElse { Properties() }))
			.addSource(PropsPropertySource(flagdContainerConfig.getOrElse { Properties() }))
			.build()
			.loadConfigOrThrow<Config>()
	}

	/**
	 * Optionally loads the MongoContainerConfig for local container-based development.
	 * It is a shared configuration between the application and the database container.
	 * The values from this file are directly passed into the database container.
	 * Thus, updating the original file will update the database container and application alike.
	 */
	private inline fun <reified T : Any> loadContainerConfig(folder: String): Validated<ConfigFailure, T> {
		val configName = T::class.simpleName
		return ConfigLoaderBuilder.default()
			// don't give a shit
			.withReportPrintFn {}
			.addParser("env", PropsParser())
			// `.env.default` file - WARNING: this assumes all *.default files are .env files
			.addParser("default", PropsParser())
			// local configuration takes precedence
			.addFileSource("infra/$folder/.env", optional = false, allowEmpty = false)
			.addFileSource("infra/$folder/.env.default", optional = true, allowEmpty = false)
			.build()
			.loadConfig<T>()
			.onFailure { logger.warn { "Failed to load $configName: ${it.description()}" } }
			.map { logger.debug { "Loaded $configName: $it" }; return@map it }
	}

	override fun loadBuildInfo(): BuildInfo = ConfigLoaderBuilder.default()
		// don't give a shit
		.withReportPrintFn {}
		.addResourceSource("/buildinfo.properties")
		.build()
		.loadConfigOrThrow<BuildInfo>()
}
