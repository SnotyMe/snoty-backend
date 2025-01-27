package me.snoty.backend.database

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import org.koin.core.Koin
import java.util.*

data class DatabaseConfig(
	val type: String,
)

fun Koin.loadDatabaseModule() {
	val logger = KotlinLogging.logger {}

	val providers = ServiceLoader.load(DatabaseProvider::class.java)
		.toList()
	logger.debug { "Loaded database providers: $providers" }

	val dbConfig: DatabaseConfig = get<ConfigLoader>().load("database") {
		providers.forEach { it.autoconfigure(this) }
	}

	val selected = providers.filter {
		dbConfig.type in it.supportedTypes
	}
	logger.debug { "Selected database config: $selected" }

	when (selected.size) {
		// just a single selected database, we can proceed
		1 -> loadModules(listOf(selected.single().koinModule))

		0 -> error("No database selected!")
		else -> error("Multiple databases selected! ($selected)")
	}
}
