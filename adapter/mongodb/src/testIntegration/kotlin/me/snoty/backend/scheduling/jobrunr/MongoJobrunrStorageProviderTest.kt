package me.snoty.backend.scheduling.jobrunr

import me.snoty.backend.database.mongo.MongoTest

class MongoJobrunrStorageProviderTest : JobrunrStorageProviderSpec() {
	private val mongoClient = MongoTest.getMongoClients {}.syncClient
	override val storageProvider = MongoJobrunrStorageProvider(mongoClient)
}
