package me.snoty.backend.featureflags

import com.sksamuel.hoplite.ConfigLoaderBuilder
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.addProperties
import me.snoty.backend.config.load
import me.snoty.backend.injection.DiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
object InMemoryKoinModule

class InMemoryOpenFeatureAdapter : FeatureFlagsAdapter {
	override val supportedTypes: List<String> = listOf("in-memory")
	override val koinModule: DiModule = InMemoryKoinModule.module()

	override fun autoconfigure(configLoader: ConfigLoaderBuilder) {
		configLoader.addProperties(
			"${FeatureFlagsAdapter.CONFIG_GROUP}.adapter" to supportedTypes.first()
		)
	}
}

data class InMemoryFeatureFlagsConfig(val flags: Map<String, String> = emptyMap())

@Single
fun provideInMemoryFeatureFlagsConfig(configLoader: ConfigLoader): InMemoryFeatureFlagsConfig =
	configLoader.load(FeatureFlagsAdapter.CONFIG_GROUP)
