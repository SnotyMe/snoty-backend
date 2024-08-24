package me.snoty.backend.wiring.node

import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import me.snoty.backend.database.mongo.Aggregations
import me.snoty.backend.database.mongo.Stages
import me.snoty.backend.database.mongo.mongoCollectionPrefix
import me.snoty.backend.database.mongo.upsertOne
import me.snoty.integration.common.wiring.Node
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePersistenceFactory
import me.snoty.integration.common.wiring.node.NodePersistenceService
import org.bson.codecs.pojo.annotations.BsonId
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

private data class NodeEntities<T>(
	@BsonId
	val _id: String,
	val entities: Map<String, T>
)

class MongoNodePersistenceService<T : Any>(
	mongoDB: MongoDatabase,
	nodeDescriptor: NodeDescriptor,
	name: String,
	private val entityClass: KClass<T>,
) : NodePersistenceService<T> {
	private val collection = mongoDB.getCollection("${nodeDescriptor.mongoCollectionPrefix}.$name", NodeEntities::class.java)

	override suspend fun persistEntity(node: Node, entityId: String, entity: T) {
		collection.upsertOne(
			Filters.eq(node._id),
			Updates.set("${NodeEntities<T>::entities.name}.$entityId", entity)
		)
	}

	override fun getEntities(node: Node): Flow<T> {
		return collection.aggregate(
			listOf(
				Aggregates.match(Filters.eq(node._id)),
				Aggregates.project(
					Projections.computed(NodeEntities<T>::entities.name,
						Stages.objectToArray(NodeEntities<T>::entities.name)
					)
				),
				Aggregations.unwind(NodeEntities<T>::entities),
				Aggregates.replaceRoot("$${NodeEntities<T>::entities.name}.v")
			),
			entityClass.java
		)
	}
}

@Single
class MongoNodePersistenceServiceFactory(private val mongoDB: MongoDatabase) : NodePersistenceFactory {
	override fun <T : Any> create(nodeDescriptor: NodeDescriptor, name: String, entityClass: KClass<T>): NodePersistenceService<T> {
		return MongoNodePersistenceService(mongoDB, nodeDescriptor, name, entityClass)
	}
}
