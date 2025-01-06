package me.snoty.backend.database.sql.migrations

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.database.sql.newSuspendedTransaction
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.koin.core.annotation.Single

@Single
class SqlMigrator(private val database: Database, private val tables: List<Table>) {
	private val logger = KotlinLogging.logger {}

	suspend fun migrate() {
		database.newSuspendedTransaction {
			var tables = tables.toTypedArray()
			SchemaUtils.createMissingTablesAndColumns(*tables)
			val migrationsRequired = SchemaUtils.statementsRequiredToActualizeScheme(*tables)
			if (migrationsRequired.isNotEmpty()) {
				logger.error {
					"""
						Migrations are required to be ran manually!
						${migrationsRequired.joinToString(separator = "\n")}
					""".trimIndent()
				}
				throw MigrationFailedException("Migrations are required to be ran manually!")
			}
		}
	}
}
