package me.snoty.backend.database.sql.migrations

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.database.sql.newSuspendedTransaction
import me.snoty.backend.injection.getFromAllScopes
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.koin.core.Koin
import org.koin.core.annotation.Single

@Single
class SqlMigrator(private val database: Database, koin: Koin) {
	private val logger = KotlinLogging.logger {}

	private val tables: List<Table> = koin.getFromAllScopes()

	suspend fun migrate() {
		database.newSuspendedTransaction {
			val sortedTables = SchemaUtils.sortTablesByReferences(tables).toTypedArray()
			SchemaUtils.createMissingTablesAndColumns(*sortedTables)
			val migrationsRequired = SchemaUtils.statementsRequiredToActualizeScheme(*sortedTables)
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
