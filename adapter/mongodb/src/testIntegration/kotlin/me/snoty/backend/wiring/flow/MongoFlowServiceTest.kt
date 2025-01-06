package me.snoty.backend.wiring.flow

import io.mockk.mockk
import me.snoty.backend.database.mongo.MongoTest
import me.snoty.backend.integration.config.MongoNodeService
import me.snoty.backend.wiring.node.NodeRegistryImpl

class MongoFlowServiceTest : FlowServiceSpec() {
	private val mongoDB = MongoTest.getMongoDatabase {}
	private val nodeRegistry = NodeRegistryImpl()
	override val nodeService = MongoNodeService(mongoDB, nodeRegistry, mockk(relaxed = true))
	override val service = MongoFlowService(mongoDB, flowScheduler, mockk(relaxed = true))
}
