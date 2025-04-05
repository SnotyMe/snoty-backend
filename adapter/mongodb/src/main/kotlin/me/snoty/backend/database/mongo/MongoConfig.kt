package me.snoty.backend.database.mongo

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.Masked
import me.snoty.backend.config.addProperties
import me.snoty.backend.config.loadContainerConfig

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

data class MongoContainerConfig(
	@ConfigAlias("MONGO_INITDB_ROOT_USERNAME")
	val username: String?,
	@ConfigAlias("MONGO_INITDB_ROOT_PASSWORD")
	val password: Masked?,
	@ConfigAlias("MONGO_PORT")
	val port: Int = 27017
)

const val MONGODB = "mongodb"

fun ConfigLoaderBuilder.autoconfigForMongo() {
	loadContainerConfig<MongoContainerConfig>("database").map {
		val properties = mutableMapOf(
			"database.type" to MONGODB,
			"mongodb.connection.type" to MongoConnectionConfig.ConnectionString::class.simpleName!!,
			"mongodb.connection.connectionString" to "mongodb://localhost:${it.port}/",
		)

		if (it.username != null && it.password != null) {
			properties += mapOf(
				"mongodb.authentication.type" to MongoAuthenticationConfig::class.simpleName!!,
				"mongodb.authentication.username" to it.username,
				"mongodb.authentication.password" to it.password.value,
			)
		}

		addProperties(properties)
	}
}
