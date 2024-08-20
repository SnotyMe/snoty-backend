package me.snoty.backend.wiring.node

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import me.snoty.backend.database.mongo.Aggregations
import me.snoty.backend.database.mongo.upsertOne
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.node.NodePersistenceService
import me.snoty.integration.common.wiring.node.NodePersistenceServiceFactory
import org.bson.codecs.pojo.annotations.BsonId
import kotlin.reflect.KClass

private data class NodeEntities(
	@BsonId
	val _id: String,
	val entities: List<Any>
)

class MongoNodePersistenceService<T : Any>(
	mongoDB: MongoDatabase,
	name: String,
	private val entityClass: KClass<T>,
) : NodePersistenceService<T> {
	private val collection = mongoDB.getCollection(name, NodeEntities::class.java)

	override suspend fun persistEntity(node: Node, entityId: String, entity: T) {
		collection.upsertOne(
			Filters.eq(node._id),
			Updates.push("entities", entity)
		)
	}

	override fun getEntities(node: Node): Flow<T> {
		return collection.aggregate(
			listOf(
				Filters.eq(node._id),
				Aggregations.unwind(NodeEntities::entities)
			),
			entityClass.java
		)
	}
}

class MongoNodePersistenceServiceFactory(private val mongoDB: MongoDatabase) : NodePersistenceServiceFactory {
	override fun <T : Any> create(name: String, entityClass: KClass<T>): NodePersistenceService<T> {
		return MongoNodePersistenceService(mongoDB, name, entityClass)
	}
}
