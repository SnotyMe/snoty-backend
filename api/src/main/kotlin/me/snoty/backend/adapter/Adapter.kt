package me.snoty.backend.adapter

import com.sksamuel.hoplite.ConfigLoaderBuilder
import org.koin.core.Koin
import org.koin.core.module.Module

interface Adapter {
	val supportedTypes: List<String>

	/**
	 * Preload function executed prior to selecting a database provider.
	 * Useful to autoconfigure from the environment.
	 */
	fun autoconfigure(configLoader: ConfigLoaderBuilder) {}

	val koinModule: Module

	data class OnLoad(val koin: Koin)
	fun onLoad(event: OnLoad) = Unit

	data class OnRegisterAlwaysOn(val koin: Koin)

	/**
	 * Register always-on components, such as additional Json Schemas or Ktor Endpoints.
	 */
	fun registerAlwaysOn(event: OnRegisterAlwaysOn) = Unit
}

val Adapter.primaryType get() = supportedTypes.firstOrNull() ?: error("Adapter $this does not support any types!")
