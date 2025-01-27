package me.snoty.backend.database.mongo

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.Masked
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.parsers.PropsPropertySource
import me.snoty.backend.config.loadContainerConfig
import java.util.*

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
	val mongoContainerConfig = loadContainerConfig<MongoContainerConfig>("database").map {
		Properties().apply {
			setProperty("database.type", MONGODB)
			setProperty("mongodb.connection.type", MongoConnectionConfig.ConnectionString::class.simpleName)
			setProperty("mongodb.connection.connectionString", "mongodb://localhost:${it.port}/")
			if (!it.username.isNullOrEmpty() || !it.username.isNullOrEmpty()) {
				setProperty("mongodb.authentication.username", it.username)
				setProperty("mongodb.authentication.password", it.password?.value)
			}
		}
	}

	addSource(PropsPropertySource(mongoContainerConfig.getOrElse { Properties() }))
}
