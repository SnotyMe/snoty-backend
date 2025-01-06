package me.snoty.backend.wiring

import me.snoty.backend.database.mongo.MongoTest
import me.snoty.backend.hooks.HookRegistryImpl
import me.snoty.backend.integration.MongoEntityStateService
import me.snoty.integration.common.diff.provideStateCodecRegistry
import me.snoty.integration.common.utils.bsonTypeClassMap
import org.bson.types.ObjectId

class MongoEntityStateServiceTest : EntityStateServiceSpec({ ObjectId().toHexString() }) {
	private val mongoDB = MongoTest.getMongoDatabase {}
	override val service = MongoEntityStateService(
		mongoDB,
		nodeDescriptor,
		hookRegistry = HookRegistryImpl(),
		codecRegistry = provideStateCodecRegistry(bsonTypeClassMap(), mongoDB.codecRegistry),
	)
}
