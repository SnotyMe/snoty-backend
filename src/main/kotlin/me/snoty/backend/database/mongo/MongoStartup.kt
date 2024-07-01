package me.snoty.backend.database.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import me.snoty.backend.config.MongoConfig
import me.snoty.integration.common.utils.integrationsApiCodecModule
import org.bson.codecs.configuration.CodecRegistries
import com.mongodb.client.MongoClient as SyncMongoClient

fun createMongoClients(config: MongoConfig, dbName: String = MONGO_DB_NAME): Pair<MongoDatabase, SyncMongoClient> {
	// val integrationCodecs = IntegrationRegistry.getIntegrationFactories().flatMap(IntegrationFactory::mongoDBCodecs)
	val mongoCodecRegistry = CodecRegistries.fromRegistries(
		// TODO: extra codecs from integrations
		// CodecRegistries.fromCodecs(integrationCodecs),
		integrationsApiCodecModule(),
		apiCodecModule()
	)

	val mongoClient = MongoClient.create(config.connectionString)
	val mongoDB = mongoClient.getDatabase(dbName).withCodecRegistry(
		mongoCodecRegistry
	)

	val mongoClientSettings = MongoClientSettings.builder()
		.codecRegistry(mongoCodecRegistry)
		.applyConnectionString(ConnectionString(config.connectionString))
		.build()
	val syncMongoClient: SyncMongoClient = MongoClients.create(mongoClientSettings)

	return Pair(mongoDB, syncMongoClient)
}
