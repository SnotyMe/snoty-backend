package me.snoty.backend.config

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import com.sksamuel.hoplite.addResourceSource
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.parsers.PropsParser
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.build.BuildInfo
import org.koin.core.annotation.Single
import java.util.*

interface ApplicationConfigLoader {
	fun loadBuildInfo(): BuildInfo
	fun loadConfig(): Config
}

@Single
class ApplicationConfigLoaderImpl(private val configLoader: ConfigLoader) : ApplicationConfigLoader {
	override fun loadBuildInfo(): BuildInfo = ConfigLoaderBuilder.saneDefault()
		// don't give a shit
		.withReportPrintFn {}
		.addResourceSource("/buildinfo.properties")
		.build()
		.loadConfigOrThrow<BuildInfo>()

	override fun loadConfig(): Config {
		val mongoContainerConfig = loadContainerConfig<MongoContainerConfig>("database").map {
			Properties().apply {
				setProperty("mongodb.connection.type", MongoConnectionConfig.ConnectionString::class.simpleName)
				setProperty("mongodb.connection.connectionString",
				            "mongodb://localhost:${it.port}/"
				)
				if (!it.username.isNullOrEmpty() || !it.username.isNullOrEmpty()) {
					setProperty("mongodb.authentication.username", it.username)
					setProperty("mongodb.authentication.password", it.password?.value)
				}
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

		return configLoader.load(prefix = null) {
			addSource(PropsPropertySource(mongoContainerConfig.getOrElse { Properties() }))
			addSource(PropsPropertySource(flagdContainerConfig.getOrElse { Properties() }))
		}
	}

	/**
	 * Optionally loads a container config for local container-based development.
	 * It is a shared configuration between the application and the database container.
	 * The values from this file are directly passed into the database container.
	 * Thus, updating the original file will update the database container and application alike.
	 */
	private inline fun <reified T : Any> loadContainerConfig(folder: String): Validated<ConfigFailure, T> {
		val logger = KotlinLogging.logger {}
		val configName = T::class.simpleName
		return ConfigLoaderBuilder.saneDefault()
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
}

@Single
fun provideConfig(configLoader: ApplicationConfigLoader) = configLoader.loadConfig()

@Single
fun provideEnvironment(config: Config) = config.environment
