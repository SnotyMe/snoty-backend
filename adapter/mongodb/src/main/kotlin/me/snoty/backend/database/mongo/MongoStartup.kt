package me.snoty.backend.database.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.sksamuel.hoplite.fp.getOrElse
import com.sksamuel.hoplite.parsers.PropsPropertySource
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.config.ConfigLoader
import me.snoty.backend.config.load
import me.snoty.backend.config.loadContainerConfig
import me.snoty.backend.database.mongo.tracing.ContextProvider
import me.snoty.backend.database.mongo.tracing.MongoTracing
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single
import java.util.Properties
import com.mongodb.client.MongoClient as SyncMongoClient
import com.mongodb.client.MongoClients as SyncMongoClients
import com.mongodb.kotlin.client.coroutine.MongoClient as CoroutineMongoClient

data class MongoClients(
	val coroutinesDatabase: MongoDatabase,
	val coroutinesClient: CoroutineMongoClient,
	val syncClient: SyncMongoClient
)

@Single
class MongoStartup(private val mongoTracing: MongoTracing) {
	fun createMongoClients(config: MongoConfig, codecRegistry: CodecRegistry, dbName: String = MONGO_DB_NAME): MongoClients {
		val logger = KotlinLogging.logger {}

		val connectionString = config.connection.buildConnectionString()

		logger.info { "Connecting to MongoDB at $connectionString"}

		fun createClientSettings(configure: MongoClientSettings.Builder.() -> Unit = {}) = MongoClientSettings.builder()
			.codecRegistry(codecRegistry)
			.applyConnectionString(ConnectionString(connectionString))
			.apply {
				config.authentication?.let {
					credential(MongoCredential.createCredential(
						it.username.value,
						it.authDatabase.value,
						it.password.value.toCharArray(),
					))
				}
			}
			.apply(configure)
			.build()

		val tracedClientSettings = createClientSettings {
			contextProvider(ContextProvider())
			addCommandListener(mongoTracing)
		}
		val mongoClient = CoroutineMongoClient.create(tracedClientSettings)
		val mongoDB = mongoClient.getDatabase(dbName)
			.withCodecRegistry(codecRegistry)

		val syncMongoClient = SyncMongoClients.create(createClientSettings())

		logger.info { "Successfully established MongoDB connection"}

		return MongoClients(mongoDB, mongoClient, syncMongoClient)
	}
}

@Single
fun provideMongoConfig(configLoader: ConfigLoader): MongoConfig = configLoader.load("mongodb") {
	val mongoContainerConfig = loadContainerConfig<MongoContainerConfig>("database").map {
		Properties().apply {
			setProperty("mongodb.connection.type", MongoConnectionConfig.ConnectionString::class.simpleName)
			setProperty("mongodb.connection.connectionString",
			            "mongodb://localhost:${it.port}/"
			)
			if (!it.username.isNullOrEmpty() || !it.username.isNullOrEmpty()) {
				setProperty("mongodb.authentication.username", it.username)
				setProperty("mongodb.authentication.password", it.password?.value)
			}
		}
	}

	addSource(PropsPropertySource(mongoContainerConfig.getOrElse { Properties() }))
}

@Single
fun provideMongoClients(mongoStartup: MongoStartup, config: MongoConfig, codecRegistry: CodecRegistry): MongoClients
	= mongoStartup.createMongoClients(config, codecRegistry)

@Single
fun provideMongoDatabase(mongoClients: MongoClients): MongoDatabase
	= mongoClients.coroutinesDatabase

@Single
fun provideSyncMongoClient(mongoClients: MongoClients): SyncMongoClient
	= mongoClients.syncClient

@Single
fun provideCoroutinesMongoClient(mongoClients: MongoClients): CoroutineMongoClient
	= mongoClients.coroutinesClient
