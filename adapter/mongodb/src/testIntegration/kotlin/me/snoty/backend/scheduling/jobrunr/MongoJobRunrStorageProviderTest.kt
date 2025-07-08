package me.snoty.backend.scheduling.jobrunr

import me.snoty.backend.database.mongo.MongoTest

class MongoJobRunrStorageProviderTest : JobRunrStorageProviderSpec() {
	private val mongoClient = MongoTest.getMongoClients {}.syncClient
	override val storageProvider = MongoJobRunrStorageProvider(mongoClient)
}
