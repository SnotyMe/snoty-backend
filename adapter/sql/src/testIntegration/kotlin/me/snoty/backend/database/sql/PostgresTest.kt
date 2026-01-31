package me.snoty.backend.database.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.snoty.backend.test.getClassNameFromBlock
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

	fun getPostgresDataSource(block: DataSource.() -> Unit): DataSource {
		val dbName = getDb(block)

		val ds = HikariDataSource(HikariConfig().apply {
			jdbcUrl = postgresContainer.jdbcUrl.replace(ADMIN_DB, dbName)
			username = postgresContainer.username
			password = postgresContainer.password
		})

		block(ds)

		return ds
	}

	fun getPostgresDatabase(block: Database.() -> Unit): Database {
		val dbName = getDb(block)

		val db = Database.connect(
			url = postgresContainer.jdbcUrl.replace(ADMIN_DB, dbName),
			user = postgresContainer.username,
			password = postgresContainer.password,
		)

		transaction(db = db) {
			block(db)
		}

		return db
	}
}
