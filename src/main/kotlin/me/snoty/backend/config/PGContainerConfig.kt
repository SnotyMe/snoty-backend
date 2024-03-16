package me.snoty.backend.config

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.Masked

/**
 * Configuration that resembles the environment variables for the PostgreSQL container.
 * The same .env file can be used for the postgres container and the application.
 * This reduces the risk of configuration drift between the two.
 */
data class PGContainerConfig(
	@ConfigAlias("POSTGRES_USER")
	val user: String,
	@ConfigAlias("POSTGRES_PASSWORD")
	val password: Masked,
	@ConfigAlias("POSTGRES_DB")
	val db: String,
	@ConfigAlias("POSTGRES_PORT")
	val port: Int = 5432
)
