package me.snoty.backend.database.mongo.migrations.impl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import me.snoty.backend.database.mongo.migrations.MongoMigration
import me.snoty.backend.wiring.flow.MongoWorkflow
import me.snoty.backend.wiring.node.MongoNode
import me.snoty.integration.common.wiring.flow.FLOW_COLLECTION_NAME
import me.snoty.integration.common.wiring.flow.NODE_COLLECTION_NAME
import org.bson.Document
import org.koin.core.annotation.Single
import kotlin.reflect.KProperty
import kotlin.time.Instant

@Single
class MigrateNodeFlowTimestamps : MongoMigration("0.8.0") {
	override val name = "MigrateNodeFlowTimestamps"

	override suspend fun execute(database: MongoDatabase) {
		val flowCollection = database.getCollection<MongoWorkflow>(FLOW_COLLECTION_NAME)
		flowCollection.updateMany(
			filter = Filters.empty(),
			update = listOf(MongoWorkflow::createdAt, MongoWorkflow::modifiedAt).mapFromId()
		)

		val nodeCollection = database.getCollection<MongoNode>(NODE_COLLECTION_NAME)
		nodeCollection.updateMany(
			filter = Filters.empty(),
			update = listOf(MongoNode::createdAt, MongoNode::modifiedAt).mapFromId()
		)
	}

	override suspend fun rollback(database: MongoDatabase) = Unit

	private val toDate = Document($$"$toDate", $$"$_id")
	private fun List<KProperty<Instant>>.mapFromId() = map {
		Document($$"$set", Document(it.name, toDate))
	}
}
