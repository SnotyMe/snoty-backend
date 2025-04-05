package me.snoty.backend.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import me.snoty.backend.build.BuildInfo
import org.koin.core.annotation.Single

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

	override fun loadConfig(): Config = configLoader.load(prefix = null) {
		loadContainerConfig<FlagdContainerConfig>("featureflags").map { containerConfig ->
			val props = mapOf(
				"type" to ProviderFeatureFlagConfig.Flagd::class.simpleName!!,
				ProviderFeatureFlagConfig.Flagd::host.name to "localhost",
				ProviderFeatureFlagConfig.Flagd::port.name to containerConfig.port.toString(),
			).mapKeys { (key, _) -> "${Config::featureFlags.name}.$key" }

			addProperties(props)
		}
	}
}

@Single
fun provideConfig(configLoader: ApplicationConfigLoader) = configLoader.loadConfig()

@Single
fun provideEnvironment(config: Config) = config.environment
