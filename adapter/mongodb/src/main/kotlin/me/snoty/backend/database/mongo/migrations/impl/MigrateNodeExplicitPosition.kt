package me.snoty.backend.database.mongo.migrations.impl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.mongodb.kotlin.client.model.Updates
import me.snoty.backend.database.mongo.migrations.MongoMigration
import me.snoty.backend.wiring.node.MongoNode
import me.snoty.integration.common.wiring.flow.NODE_COLLECTION_NAME
import me.snoty.integration.common.wiring.node.NodePosition
import org.koin.core.annotation.Single

@Single
class MigrateNodeExplicitPosition : MongoMigration("0.8.0") {
	override val name = "MigrateNodeExplicitPosition"

	override suspend fun execute(database: MongoDatabase) {
		val collection = database.getCollection<MongoNode>(NODE_COLLECTION_NAME)
		collection.updateMany(
			filter = Filters.empty(),
			update = Updates.set(MongoNode::position, NodePosition(0, 0, 250, 150))
		)
	}

	override suspend fun rollback(database: MongoDatabase) = Unit
}
