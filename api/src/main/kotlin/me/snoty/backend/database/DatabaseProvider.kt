package me.snoty.backend.database

import com.sksamuel.hoplite.ConfigLoaderBuilder
import org.koin.core.module.Module

interface DatabaseProvider {
	val supportedTypes: List<String>

	/**
	 * Preload function executed prior to selecting a database provider.
	 * Useful to autoconfigure from the environment.
	 */
	fun autoconfigure(configLoader: ConfigLoaderBuilder) {}

	val koinModule: Module
}
