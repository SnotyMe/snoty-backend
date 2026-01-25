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
import me.snoty.backend.database.DatabaseAdapter
import org.koin.core.annotation.Single
import javax.sql.DataSource

const val SQL_ADAPTER_TYPE = "sql"
private const val CONFIG_KEY = "${DatabaseAdapter.CONFIG_GROUP}.${SQL_ADAPTER_TYPE}"

class SqlAdapter : DatabaseAdapter {
	override val supportedTypes: List<String> = listOf(SQL_ADAPTER_TYPE, "postgres")
	override val koinModule = sqlKoinModule

	override fun autoconfigure(configLoader: ConfigLoaderBuilder) = configLoader.autoconfigForSql()
}

/**
 * Workaround because hoplite "can only decode into data classes"
 */
data class SqlConfigWrapper(
	val sql: HikariDataSource
)

@Single
fun provideDataSource(configLoader: ConfigLoader, openTelemetry: OpenTelemetry): DataSource =
	configLoader.load<SqlConfigWrapper>(DatabaseAdapter.CONFIG_GROUP) {
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
	"${CONFIG_KEY}.leakDetectionThreshold" to 10000
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
			"${DatabaseAdapter.CONFIG_GROUP}.adapter" to "sql",
			"${CONFIG_KEY}.username" to it.user,
			"${CONFIG_KEY}.password" to it.password.value,
			"${CONFIG_KEY}.jdbcUrl" to "jdbc:postgresql://localhost:${it.port}/${it.db}",
		))
	}
}
