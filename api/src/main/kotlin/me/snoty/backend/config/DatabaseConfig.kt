package me.snoty.backend.config

import com.sksamuel.hoplite.Masked

data class MongoConfig(val connection: MongoConnectionConfig, val authentication: MongoAuthenticationConfig? = null)

sealed class MongoConnectionConfig {
	data class ConnectionString(val connectionString: String) : MongoConnectionConfig() {
		override fun buildConnectionString() = connectionString
	}

	data class Split(
		val srv: Boolean = false,
		val host: String,
		val port: Int? = 27017,
		val database: String = "snoty",
		val additionalOptions: String = "",
	) : MongoConnectionConfig() {
		override fun buildConnectionString() = buildString {
			append("mongodb")
			if (srv) append("+srv")
			append("://$host")
			// srv connections don't use port - they get it from the DNS record
			if (port != null && !srv) append(":$port")
			append("/$database")
			if (additionalOptions.isNotEmpty()) append("?$additionalOptions")
		}
	}

	abstract fun buildConnectionString(): String
}

data class MongoAuthenticationConfig(
	val authDatabase: Masked = Masked("admin"),
	val username: Masked,
	val password: Masked,
)
