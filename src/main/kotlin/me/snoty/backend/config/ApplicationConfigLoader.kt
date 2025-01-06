package me.snoty.backend.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.parsers.PropsPropertySource
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
		val flagdContainerConfig = loadContainerConfig<FlagdContainerConfig>("featureflags").map {
			Properties().apply {
				val prefix = Config::featureFlags.name + "."
				setProperty(prefix + "type", ProviderFeatureFlagConfig.Flagd::class.simpleName)
				setProperty(prefix + ProviderFeatureFlagConfig.Flagd::host.name, "localhost")
				setProperty(prefix + ProviderFeatureFlagConfig.Flagd::port.name, it.port.toString())
			}
		}

		return configLoader.load(prefix = null) {
			addSource(PropsPropertySource(flagdContainerConfig.getOrElse { Properties() }))
		}
	}
}

@Single
fun provideConfig(configLoader: ApplicationConfigLoader) = configLoader.loadConfig()

@Single
fun provideEnvironment(config: Config) = config.environment
