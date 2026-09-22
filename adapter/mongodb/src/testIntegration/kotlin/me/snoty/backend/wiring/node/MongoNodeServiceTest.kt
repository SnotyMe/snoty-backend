package me.snoty.backend.wiring.node

import me.snoty.backend.database.mongo.MongoTest
import me.snoty.backend.database.mongo.toFlowId
import me.snoty.core.node.NodeType
import org.bson.Document
import org.bson.types.ObjectId

class MongoNodeServiceTest : NodeServiceSpec() {
	private val db = MongoTest.getMongoDatabase {}
	override val service = MongoNodeService(db, object : NodeSettingsDeserializationService {
		override fun deserializeOrInvalid(
			nodeType: NodeType,
			nodeSettings: Document
		): NodeSettings = EmptyNodeSettings()
	})

	override val makeFlowId = suspend { ObjectId().toFlowId() }
}
