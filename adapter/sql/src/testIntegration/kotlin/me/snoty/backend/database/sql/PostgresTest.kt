package me.snoty.backend.database.sql

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container

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

	fun getPostgresDatabase(block: () -> Unit): Database {
		val javaClass = block.javaClass
		var name = javaClass.name
		name = when {
			name.contains("Kt$") -> name.substringBefore("Kt$")
			name.contains("$") -> name.substringBefore("$")
			else -> name
		}.substringAfterLast(".")

		var dbName = "${name}_${javaClass.hashCode()}".lowercase()
		transaction(adminClient) {
			// autoCommit is required when using postgres
			connection.autoCommit = true
			SchemaUtils.createDatabase(dbName)
			connection.autoCommit = false
		}

		return Database.connect(
			url = postgresContainer.jdbcUrl.replace(ADMIN_DB, dbName),
			user = postgresContainer.username,
			password = postgresContainer.password,
		)
	}
}
