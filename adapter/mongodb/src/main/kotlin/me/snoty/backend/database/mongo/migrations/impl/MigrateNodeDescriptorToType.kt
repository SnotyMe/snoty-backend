package me.snoty.backend.database.mongo.migrations.impl

import com.mongodb.MongoNamespace
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.mongodb.kotlin.client.model.Filters
import kotlinx.coroutines.flow.filter
import me.snoty.backend.database.mongo.EMPTY
import me.snoty.backend.database.mongo.NODE_COLLECTION_NAME
import me.snoty.backend.database.mongo.migrations.MongoMigration
import me.snoty.backend.database.mongo.mongoCollectionPrefix
import me.snoty.backend.wiring.node.MongoNode
import me.snoty.core.node.NodeType
import org.bson.Document
import org.koin.core.annotation.Single

@Single
class MigrateNodeDescriptorToType : MongoMigration("0.8.0") {
	override val name = "NodeDescriptorToType"

	override suspend fun execute(database: MongoDatabase) {
		val collection = database.getCollection<MongoNode>(NODE_COLLECTION_NAME)

		collection.updateMany(
			Filters.EMPTY,
			listOf(
				Updates.set(MongoNode::type.name, $$"$descriptor.name")
			)
		)

		val collectionNameRegex = Regex("^nodes:[^:]+:([^:]+):([^:]+)$")
		database.listCollectionNames()
			.filter { it.startsWith("nodes:") }
			.collect {
				val collection = database.getCollection<Document>(it)
				val groups = collectionNameRegex.matchEntire(it)?.groups
					?: error("Collection doesn't match expected pattern: $it")
				val type = groups[1]?.value?.let(::NodeType) ?: error("Collection doesn't have a node type: $it")
				val subname = groups[2]?.value ?: error("Collection doesn't have a subname: $it")
				collection.renameCollection(
					MongoNamespace(
						database.name,
						type.mongoCollectionPrefix + ":" + subname
					)
				)
			}
	}

	// rollback not supported as we would need a mapping to determine the namespace
	override suspend fun rollback(database: MongoDatabase) = Unit
}
