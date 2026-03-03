package me.snoty.backend.database.sql.migrations

import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.build.BuildInfo
import me.snoty.backend.database.sql.schema
import me.snoty.backend.database.sql.suspendTransaction
import me.snoty.backend.database.utils.SqlTableRegistry
import me.snoty.backend.injection.getFromAllScopes
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import org.koin.core.Koin
import org.koin.core.annotation.Single
import javax.sql.DataSource

@Single
class SqlMigrator(
	private val dataSource: DataSource,
	private val database: Database,
	private val buildInfo: BuildInfo,
	private val koin: Koin,
	private val sqlTableRegistry: SqlTableRegistry,
) {
	private val logger = KotlinLogging.logger {}

	suspend fun migrate() {
		logger.info { "Migrating database using Flyway..." }
		val flyway = Flyway.configure()
			.loggers("slf4j")
			.dataSource(dataSource)
			.defaultSchema(dataSource.schema)
			.target("${buildInfo.version}.${"9".repeat(10)}?")
			.javaMigrations(*koin.getFromAllScopes<BaseJavaMigration>().toTypedArray())
			.validateMigrationNaming(true)
			.load()

		val migrationResult = flyway.migrate()
		logger.info { "Database migration complete. ${migrationResult.migrationsExecuted} Migrations applied. Verifying schema..." }

		database.suspendTransaction {
			val tables: List<Table> = sqlTableRegistry.getTables()
			val required = MigrationUtils.statementsRequiredForDatabaseMigration(*tables.toTypedArray())
			check(required.isEmpty()) {
				"Database schema is not up to date. Migration statements required:\n${required.joinToString("\n")}"
			}
		}

		logger.info { "Database schema is up to date!" }
	}
}
