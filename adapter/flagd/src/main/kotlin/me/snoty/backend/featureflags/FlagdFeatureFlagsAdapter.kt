package me.snoty.backend.featureflags

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.ConfigLoaderBuilder
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.addProperties
import me.snoty.backend.config.load
import me.snoty.backend.config.loadContainerConfig
import me.snoty.backend.injection.DiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

const val FLAGD_ADAPTER_TYPE = "flagd"
private const val CONFIG_KEY = "${FeatureFlagsAdapter.CONFIG_GROUP}.${FLAGD_ADAPTER_TYPE}"

@Module
@ComponentScan
object FlagdKoinModule

class FlagdFeatureFlagsAdapter : FeatureFlagsAdapter {
	override val supportedTypes: List<String> = listOf(FLAGD_ADAPTER_TYPE)
	override val koinModule: DiModule = FlagdKoinModule.module()

	override fun autoconfigure(configLoader: ConfigLoaderBuilder) {
		autoconfigForFlagd(configLoader)
	}
}

data class FlagdFeatureFlagsConfig(val host: String, val port: Short)
data class FlagdContainerConfig(
	@ConfigAlias("FLAGD_PORT")
	val port: Short
)

private fun autoconfigForFlagd(configLoader: ConfigLoaderBuilder) =
	loadContainerConfig<FlagdContainerConfig>("featureflags").map { containerConfig ->
		val props = mapOf(
			"adapter" to FLAGD_ADAPTER_TYPE.first(),
			FlagdFeatureFlagsConfig::host.name to "localhost",
			FlagdFeatureFlagsConfig::port.name to containerConfig.port.toString(),
		).mapKeys { (key, _) -> "${CONFIG_KEY}.$key" }

		configLoader.addProperties(props)
	}

@Single
fun provideFlagdFeatureFlagsConfig(configLoader: ConfigLoader): FlagdFeatureFlagsConfig =
	configLoader.load(CONFIG_KEY, ::autoconfigForFlagd)
