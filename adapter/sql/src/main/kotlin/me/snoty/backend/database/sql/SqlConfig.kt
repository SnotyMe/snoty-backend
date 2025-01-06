package me.snoty.backend.database.sql

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.Masked
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.parsers.PropsPropertySource
import com.zaxxer.hikari.HikariDataSource
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import me.snoty.backend.config.loadContainerConfig
import org.koin.core.annotation.Single
import java.util.*
import javax.sql.DataSource

/**
 * Workaround because hoplite "can only decode into data classes"
 */
data class SqlConfigWrapper(
	val sql: HikariDataSource
)

@Single(binds = [HikariDataSource::class, DataSource::class])
fun provideDataSource(configLoader: ConfigLoader): HikariDataSource = configLoader.load<SqlConfigWrapper>(null) {
	val postgresContainerConfig = loadContainerConfig<PostgresContainerConfig>("database").map {
		Properties().apply {
			setProperty("sql.username", it.user)
			setProperty("sql.password", it.password.value)
			setProperty("sql.jdbcUrl", "jdbc:postgresql://localhost:${it.port}/${it.db}")
		}
	}

	postgresContainerConfig.getOrElse { null }?.let {
		addSource(PropsPropertySource(it))
	}
}.sql


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
