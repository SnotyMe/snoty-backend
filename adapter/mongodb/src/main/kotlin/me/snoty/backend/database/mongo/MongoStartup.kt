package me.snoty.backend.database.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging
import me.snoty.backend.config.Config
import me.snoty.backend.config.MongoConfig
import me.snoty.backend.database.mongo.tracing.ContextProvider
import me.snoty.backend.database.mongo.tracing.MongoTracing
import me.snoty.integration.common.utils.bsonTypeClassMap
import me.snoty.integration.common.utils.integrationsApiCodecModule
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single
import com.mongodb.client.MongoClient as SyncMongoClient
import com.mongodb.client.MongoClients as SyncMongoClients
import com.mongodb.kotlin.client.coroutine.MongoClient as CoroutineMongoClient

data class MongoClients(
	val coroutinesDatabase: MongoDatabase,
	val syncClient: SyncMongoClient
)

@Single
class MongoStartup(private val mongoTracing: MongoTracing) {
	fun createMongoClients(config: MongoConfig, dbName: String = MONGO_DB_NAME): MongoClients {
		val logger = KotlinLogging.logger {}
		val mongoCodecRegistry = CodecRegistries.fromRegistries(
			// TODO: extra codecs from integrations
			integrationsApiCodecModule(bsonTypeClassMap()),
			apiCodecModule()
		)

		val connectionString = config.connection.buildConnectionString()

		logger.info { "Connecting to MongoDB at $connectionString"}

		fun createClientSettings(configure: MongoClientSettings.Builder.() -> Unit = {}) = MongoClientSettings.builder()
			.codecRegistry(mongoCodecRegistry)
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
		val mongoDB = mongoClient.getDatabase(dbName).withCodecRegistry(
			mongoCodecRegistry
		)

		val syncMongoClient = SyncMongoClients.create(createClientSettings())

		logger.info { "Successfully established MongoDB connection"}

		return MongoClients(mongoDB, syncMongoClient)
	}
}

@Single
fun provideMongoClients(mongoStartup: MongoStartup, config: Config): MongoClients
	= mongoStartup.createMongoClients(config.mongodb)

@Single
fun provideMongoDatabase(mongoClients: MongoClients): MongoDatabase
	= mongoClients.coroutinesDatabase

@Single
fun provideSyncMongoClient(mongoClients: MongoClients): SyncMongoClient
	= mongoClients.syncClient

@Single
fun provideCodecRegistry(mongoDatabase: MongoDatabase): CodecRegistry
	= mongoDatabase.codecRegistry
