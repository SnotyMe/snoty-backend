package me.snoty.backend.wiring

import io.mockk.mockk
import me.snoty.backend.database.mongo.MongoTest
import me.snoty.backend.hooks.HookRegistryImpl
import me.snoty.backend.integration.MongoEntityStateService
import me.snoty.backend.integration.config.MongoNodeService
import me.snoty.backend.wiring.flow.MongoFlowService
import me.snoty.integration.common.config.NodeService
import me.snoty.integration.common.diff.provideStateCodecRegistry
import me.snoty.integration.common.utils.bsonTypeClassMap
import me.snoty.integration.common.wiring.flow.FlowService
import org.bson.types.ObjectId

class MongoEntityStateServiceTest : EntityStateServiceSpec({ ObjectId().toHexString() }) {
	private val mongoDB = MongoTest.getMongoDatabase {}
	override val service = MongoEntityStateService(
		mongoDB,
		nodeDescriptor,
		hookRegistry = HookRegistryImpl(),
		codecRegistry = provideStateCodecRegistry(bsonTypeClassMap(), mongoDB.codecRegistry),
	)
	override val nodeService: NodeService = MongoNodeService(mongoDB, mockk(), mockk())
	override val flowService: FlowService = MongoFlowService(mongoDB, mockk(relaxed = true), mockk(relaxed = true))
}
