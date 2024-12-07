package me.snoty.backend.config

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLoader as HopliteConfigLoader
import me.snoty.backend.config.ConfigLoader as SnotyConfigLoader
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.fp.getOrElse

interface ConfigLoader {
	fun build(configure: ConfigLoaderBuilder.() -> Unit): HopliteConfigLoader
}

/**
 * Describes wrapper data classes that just have one key, for example in order to do sealed loading.
 */
@Retention
annotation class ConfigWrapper

class ConfigException(val fail: ConfigFailure) : RuntimeException(fail.description())

inline fun <reified C : Any> SnotyConfigLoader.load(prefix: String?, noinline configure: ConfigLoaderBuilder.() -> Unit = {}): C
	= build(configure).loadConfig<C>(prefix = prefix).getOrElse { failure ->
		throw ConfigException(failure)
	}

// doesn't include the stupid `PathNormalizer` that broke the 2.8.0.RC3 -> 2.8.0 upgrade
fun ConfigLoaderBuilder.Companion.saneDefault() =
	empty()
		.addDefaultDecoders()
		.addDefaultPreprocessors()
		.addDefaultParamMappers()
		.addDefaultPropertySources()
		.addDefaultParsers()
