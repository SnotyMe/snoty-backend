package me.snoty.backend.wiring.flow

import me.snoty.backend.database.mongo.MongoTest
import me.snoty.backend.integration.config.MongoNodeService
import me.snoty.backend.wiring.node.NodeServiceSpec
import me.snoty.integration.common.wiring.node.EmptyNodeSettings

class MongoNodeServiceTest : NodeServiceSpec() {
	private val db = MongoTest.getMongoDatabase {}
	override val service = MongoNodeService(db, nodeRegistry) { _, _ ->
		EmptyNodeSettings()
	}
}
