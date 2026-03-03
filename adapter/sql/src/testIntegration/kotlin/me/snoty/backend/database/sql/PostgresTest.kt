package me.snoty.backend.database.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.snoty.backend.test.getClassNameFromBlock
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.postgresql.PostgreSQLContainer
import javax.sql.DataSource

object PostgresTest {
	const val ADMIN_DB = "admin"

	@Container
	val postgresContainer = PostgreSQLContainer("postgres:17")
		.withDatabaseName(ADMIN_DB)!!

	val adminClient: Database

	init {
		postgresContainer.start()
		adminClient = Database.connect(
			url = postgresContainer.jdbcUrl,
			user = postgresContainer.username,
			password = postgresContainer.password,
		)
	}

	fun <T> getDb(block: T.() -> Unit): String {
		val name = getClassNameFromBlock(block)

		val dbName = "${name}_${block.javaClass.hashCode()}".lowercase()
		transaction(adminClient) {
			// autoCommit is required when using postgres
			connection.autoCommit = true
			SchemaUtils.createDatabase(dbName)
			connection.autoCommit = false
		}

		return dbName
	}

	fun getPostgresDataSource(block: Unit.() -> Unit): DataSource =
		getPostgresDataSource(getDb(block))

	fun getPostgresDatabase(extraMigrations: List<BaseJavaMigration> = emptyList(), block: Database.() -> Unit): Database {
		val dbName = getDb(block)

		val dataSource = getPostgresDataSource(dbName)
		val db = Database.connect(dataSource)

		val flyway = Flyway.configure()
			.loggers("slf4j")
			.dataSource(dataSource)
			.defaultSchema(dataSource.schema)
			.javaMigrations(*extraMigrations.toTypedArray())
			.validateMigrationNaming(true)
			.load()

		flyway.migrate()

		transaction(db = db) {
			block(db)
		}

		return db
	}

	private fun getPostgresDataSource(dbName: String): DataSource = HikariDataSource(HikariConfig().apply {
        this.jdbcUrl = postgresContainer.jdbcUrl.replace(ADMIN_DB, dbName)
        this.username = postgresContainer.username
        this.password = postgresContainer.password
    })
}
