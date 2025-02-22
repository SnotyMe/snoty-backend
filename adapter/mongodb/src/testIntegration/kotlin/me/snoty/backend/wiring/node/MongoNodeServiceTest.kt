package me.snoty.backend.wiring.node

import me.snoty.backend.database.mongo.MongoTest
import me.snoty.backend.integration.config.MongoNodeService
import me.snoty.integration.common.wiring.node.EmptyNodeSettings
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.types.ObjectId
import kotlin.reflect.KClass

class MongoNodeServiceTest : NodeServiceSpec() {
	private val db = MongoTest.getMongoDatabase {}
	override val service = MongoNodeService(db, nodeRegistry, object : NodeSettingsDeserializationService {
		override fun deserialize(
			nodeDescriptor: NodeDescriptor,
			nodeSettings: Document,
			settingsClassOverride: KClass<out NodeSettings>?
		): NodeSettings = EmptyNodeSettings()
	})

	override val makeId = suspend { ObjectId().toHexString() }
}
