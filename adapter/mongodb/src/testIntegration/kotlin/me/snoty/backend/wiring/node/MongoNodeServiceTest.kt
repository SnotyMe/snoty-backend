package me.snoty.backend.wiring.node

import me.snoty.backend.database.mongo.MongoTest
import me.snoty.backend.integration.config.MongoNodeService
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.types.ObjectId

class MongoNodeServiceTest : NodeServiceSpec() {
	private val db = MongoTest.getMongoDatabase {}
	override val service = MongoNodeService(db, object : NodeSettingsDeserializationService {
		override fun deserializeOrInvalid(
			nodeDescriptor: NodeDescriptor,
			nodeSettings: Document
		): NodeSettings = EmptyNodeSettings(nodeSettings.getString(NodeSettings::name.name))
	})

	override val makeId = suspend { ObjectId().toHexString() }
}
