package me.snoty.backend.wiring.flow

import io.mockk.mockk
import me.snoty.backend.database.mongo.MongoTest
import me.snoty.backend.database.mongo.toFlowId
import me.snoty.backend.integration.config.MongoNodeService
import org.bson.types.ObjectId

class MongoFlowServiceTest : FlowServiceSpec({ ObjectId().toFlowId() }) {
	private val mongoDB = MongoTest.getMongoDatabase {}
	override val nodeService = MongoNodeService(mongoDB, mockk(relaxed = true))
	override val service = MongoFlowService(mongoDB, flowScheduler, mockk(relaxed = true))
}
