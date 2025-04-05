package me.snoty.backend.database.sql

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.Masked
import com.zaxxer.hikari.HikariDataSource
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.addProperties
import me.snoty.backend.config.load
import me.snoty.backend.config.loadContainerConfig
import org.koin.core.annotation.Single
import javax.sql.DataSource

/**
 * Workaround because hoplite "can only decode into data classes"
 */
data class SqlConfigWrapper(
	val sql: HikariDataSource
)

@Single
fun provideDataSource(configLoader: ConfigLoader, openTelemetry: OpenTelemetry): DataSource = configLoader.load<SqlConfigWrapper>(null) {
	defaultConfig()
	autoconfigForSql()
}
	.sql
	.let {
		JdbcTelemetry
			.create(openTelemetry)
			.wrap(it)
	}

fun ConfigLoaderBuilder.defaultConfig() = addProperties(mapOf(
	"sql.leakDetectionThreshold" to 10000
))

/**
 * Configuration that resembles the environment variables for the PostgreSQL container.
 * The same .env file can be used for the postgres container and the application.
 * This reduces the risk of configuration drift between the two.
 */
data class PostgresContainerConfig(
	@ConfigAlias("POSTGRES_USER")
	val user: String,
	@ConfigAlias("POSTGRES_PASSWORD")
	val password: Masked,
	@ConfigAlias("POSTGRES_DB")
	val db: String,
	@ConfigAlias("POSTGRES_PORT")
	val port: Int = 5432
)

fun ConfigLoaderBuilder.autoconfigForSql() {
	loadContainerConfig<PostgresContainerConfig>("database").map {
		addProperties(mapOf(
			"database.type" to "sql",
			"sql.username" to it.user,
			"sql.password" to it.password.value,
			"sql.jdbcUrl" to "jdbc:postgresql://localhost:${it.port}/${it.db}",
		))
	}
}
