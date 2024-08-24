package me.snoty.backend.database.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients as SyncMongoClients
import com.mongodb.client.MongoClient as SyncMongoClient
import com.mongodb.kotlin.client.coroutine.MongoClient as CoroutineMongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import me.snoty.backend.config.Config
import me.snoty.backend.config.MongoConfig
import me.snoty.integration.common.utils.integrationsApiCodecModule
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.koin.core.annotation.Single

data class MongoClients(
	val coroutinesDatabase: MongoDatabase,
	val syncClient: SyncMongoClient
)

fun createMongoClients(config: MongoConfig, dbName: String = MONGO_DB_NAME): MongoClients {
	val mongoCodecRegistry = CodecRegistries.fromRegistries(
		// TODO: extra codecs from integrations
		integrationsApiCodecModule(),
		apiCodecModule()
	)

	val mongoClient = CoroutineMongoClient.create(config.connectionString)
	val mongoDB = mongoClient.getDatabase(dbName).withCodecRegistry(
		mongoCodecRegistry
	)

	val mongoClientSettings = MongoClientSettings.builder()
		.codecRegistry(mongoCodecRegistry)
		.applyConnectionString(ConnectionString(config.connectionString))
		.build()
	val syncMongoClient = SyncMongoClients.create(mongoClientSettings)

	return MongoClients(mongoDB, syncMongoClient)
}

@Single
fun provideMongoClients(config: Config): MongoClients
	= createMongoClients(config.mongodb)

@Single
fun provideMongoDatabase(mongoClients: MongoClients): MongoDatabase
	= mongoClients.coroutinesDatabase

@Single
fun provideSyncMongoClient(mongoClients: MongoClients): SyncMongoClient
	= mongoClients.syncClient

@Single
fun provideCodecRegistry(mongoDatabase: MongoDatabase): CodecRegistry
	= mongoDatabase.codecRegistry
