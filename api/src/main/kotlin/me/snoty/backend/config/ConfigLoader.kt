package me.snoty.backend.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import kotlin.reflect.KClass

interface ConfigLoader {
	fun <C : Any> load(prefix: String?, clazz: KClass<C>, configure: ConfigLoaderBuilder.() -> Unit): C
}

inline fun <reified C : Any> ConfigLoader.load(prefix: String?, noinline configure: ConfigLoaderBuilder.() -> Unit = {})
	= load(prefix, C::class, configure)

// doesn't include the stupid `PathNormalizer` that broke the 2.8.0.RC3 -> 2.8.0 upgrade
fun ConfigLoaderBuilder.Companion.saneDefault() =
	empty()
		.addDefaultDecoders()
		.addDefaultPreprocessors()
		.addDefaultParamMappers()
		.addDefaultPropertySources()
		.addDefaultParsers()
