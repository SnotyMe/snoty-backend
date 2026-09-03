package me.snoty.backend.database.mongo.migrations.impl

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import me.snoty.backend.database.mongo.migrations.MongoMigration
import me.snoty.backend.wiring.node.MongoNode
import me.snoty.integration.common.wiring.flow.NODE_COLLECTION_NAME
import org.koin.core.annotation.Single

@Single
class MigrateNodeNameTopLevel : MongoMigration("0.8.0") {
	override val name: String = "NodeNameTopLevel"
	override val description: String = "move node name from settings to top-level"

	override suspend fun execute(database: MongoDatabase) {
		val collection = database.getCollection<MongoNode>(NODE_COLLECTION_NAME)
		collection.updateMany(
			Filters.empty(),
			listOf(
				Updates.set(MongoNode::name.name, "$${MongoNode::settings.name}.${MongoNode::name.name}"),
			)
		)
	}

	override suspend fun rollback(database: MongoDatabase) =
		// The name field is not dropped in this migration yet, so we don't need to rollback anything
		Unit
}
