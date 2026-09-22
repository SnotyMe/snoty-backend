package me.snoty.backend.wiring

import io.mockk.mockk
import me.snoty.backend.database.mongo.MongoTest
import me.snoty.backend.database.mongo.toNodeId
import me.snoty.backend.utils.bson.bsonTypeClassMap
import me.snoty.backend.wiring.flow.FlowService
import me.snoty.backend.wiring.flow.MongoFlowService
import me.snoty.backend.wiring.node.MongoNodeService
import me.snoty.backend.wiring.node.NodeService
import me.snoty.backend.wiring.node.state.MongoEntityStateService
import me.snoty.backend.wiring.node.state.provideStateCodecRegistry
import org.bson.types.ObjectId

class MongoEntityStateServiceTest : EntityStateServiceSpec({ ObjectId().toNodeId() }) {
	private val mongoDB = MongoTest.getMongoDatabase {}
	override val service = MongoEntityStateService(
		mongoDB,
		nodeType,
		hookRegistry = mockk(relaxed = true),
		codecRegistry = provideStateCodecRegistry(bsonTypeClassMap(), mongoDB.codecRegistry),
	)
	override val nodeService: NodeService = MongoNodeService(mongoDB, mockk())
	override val flowService: FlowService = MongoFlowService(mongoDB, mockk(relaxed = true), mockk(relaxed = true))
}
