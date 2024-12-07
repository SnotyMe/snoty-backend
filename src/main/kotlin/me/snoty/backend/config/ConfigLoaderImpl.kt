package me.snoty.backend.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ExperimentalHoplite
import com.sksamuel.hoplite.addEnvironmentSource
import com.sksamuel.hoplite.addFileSource
import org.koin.core.annotation.Single

@Single
class ConfigLoaderImpl : ConfigLoader {
	@OptIn(ExperimentalHoplite::class)
	override fun build(configure: ConfigLoaderBuilder.() -> Unit) = ConfigLoaderBuilder.saneDefault()
		// don't give a shit
		.withReportPrintFn {}
		.withResolveTypesCaseInsensitive()
		.withExplicitSealedTypes("type")
		.addDefaultPreprocessors()
		.addEnvironmentSource(useUnderscoresAsSeparator = false)
		.addFileSource("application.local.yml", optional = true)
		.addFileSource("application.yml", optional = true)
		.apply(configure)
		.build()
}
