package me.snoty.backend.database.mongo

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import me.snoty.backend.config.MongoConfig
import me.snoty.backend.spi.IntegrationRegistry
import me.snoty.integration.common.IntegrationFactory
import me.snoty.integration.common.utils.integrationsApiCodecModule
import org.bson.codecs.configuration.CodecRegistries
import com.mongodb.client.MongoClient as SyncMongoClient

fun createMongoClients(config: MongoConfig): Pair<MongoDatabase, SyncMongoClient> {
	val integrationCodecs = IntegrationRegistry.getIntegrationFactories().flatMap(IntegrationFactory::mongoDBCodecs)
	val mongoCodecRegistry = CodecRegistries.fromRegistries(
		CodecRegistries.fromCodecs(integrationCodecs),
		integrationsApiCodecModule(),
		apiCodecModule()
	)


	val client = MongoClient.create(config.connectionString)
	val snotyDB = client.getDatabase(MONGO_DB_NAME).withCodecRegistry(
		mongoCodecRegistry
	)

	val mongoClientSettings = MongoClientSettings.builder()
		.codecRegistry(mongoCodecRegistry)
		.applyConnectionString(ConnectionString(config.connectionString))
		.build()
	val syncMongoClient: SyncMongoClient = MongoClients.create(mongoClientSettings)

	return Pair(snotyDB, syncMongoClient)
}
