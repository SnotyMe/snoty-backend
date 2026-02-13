package me.snoty.backend.database.mongo

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.Masked
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.addProperties
import me.snoty.backend.config.load
import me.snoty.backend.config.loadContainerConfig
import me.snoty.backend.database.DatabaseAdapter
import me.snoty.backend.injection.DiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

const val MONGODB_ADAPTER_TYPE = "mongodb"
private const val CONFIG_KEY = "${DatabaseAdapter.CONFIG_GROUP}.${MONGODB_ADAPTER_TYPE}"

@Module
@ComponentScan("me.snoty.backend")
object MongoKoinModule

class MongoAdapter : DatabaseAdapter {
	override val supportedTypes = listOf(MONGODB_ADAPTER_TYPE)
	override val koinModule: DiModule = MongoKoinModule.module()

	override fun autoconfigure(configLoader: ConfigLoaderBuilder) = configLoader.autoconfigForMongo()
}

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

fun ConfigLoaderBuilder.autoconfigForMongo() {
	loadContainerConfig<MongoContainerConfig>("database").map {
		val properties = mutableMapOf(
			"${DatabaseAdapter.CONFIG_GROUP}.adapter" to MONGODB_ADAPTER_TYPE,
			"${CONFIG_KEY}.connection.type" to MongoConnectionConfig.ConnectionString::class.simpleName!!,
			"${CONFIG_KEY}.connection.connectionString" to "mongodb://localhost:${it.port}/",
		)

		if (it.username != null && it.password != null) {
			properties += mapOf(
				"${CONFIG_KEY}.authentication.type" to MongoAuthenticationConfig::class.simpleName!!,
				"${CONFIG_KEY}.authentication.username" to it.username,
				"${CONFIG_KEY}.authentication.password" to it.password.value,
			)
		}

		addProperties(properties)
	}
}

@Single
fun provideMongoConfig(configLoader: ConfigLoader): MongoConfig = configLoader.load(CONFIG_KEY) {
	autoconfigForMongo()
}
