package me.snoty.backend.database.sql

import com.sksamuel.hoplite.ConfigLoaderBuilder
import me.snoty.backend.database.DatabaseAdapter

class SqlAdapter : DatabaseAdapter {
	override val supportedTypes: List<String> = listOf("sql", "postgres")
	override val koinModule = sqlKoinModule

	override fun autoconfigure(configLoader: ConfigLoaderBuilder) = configLoader.autoconfigForSql()
}
