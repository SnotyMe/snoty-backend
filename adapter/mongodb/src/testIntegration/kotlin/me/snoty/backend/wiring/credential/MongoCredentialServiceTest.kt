package me.snoty.backend.wiring.credential

import me.snoty.backend.database.mongo.MongoTest
import me.snoty.backend.test.TestCodecRegistry
import org.bson.types.ObjectId

class MongoCredentialServiceTest : CredentialServiceSpec({ ObjectId().toHexString() }) {
	private val mongoDB = MongoTest.getMongoDatabase {}

	override val service: CredentialService = MongoCredentialService(
		mongoDB,
		registry = credentialRegistry,
		authenticationProvider = authenticationProvider,
		codecRegistry = TestCodecRegistry,
	)
}
