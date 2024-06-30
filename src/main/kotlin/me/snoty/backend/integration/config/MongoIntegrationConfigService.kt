package me.snoty.backend.integration.config

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.snoty.backend.database.mongo.decode
import me.snoty.backend.database.mongo.encode
import me.snoty.backend.integration.flow.model.graph.GraphNode
import me.snoty.integration.common.IntegrationConfig
import me.snoty.integration.common.IntegrationSettings
import me.snoty.integration.common.config.IntegrationConfigService
import me.snoty.integration.common.flow.FLOW_COLLECTION_NAME
import org.bson.Document
import java.util.*
import kotlin.reflect.KClass

class MongoIntegrationConfigService(db: MongoDatabase) : IntegrationConfigService {
	private val collection = db.getCollection<GraphNode>(FLOW_COLLECTION_NAME)

	override fun <S : IntegrationSettings> getAll(integrationType: String, clazz: KClass<S>): Flow<IntegrationConfig<S>> {
		// the UntypedIntegrationConfig is needed because the generics of generics are erased
		// as the kotlin driver just eventually uses java's `Class` in the background
		// which, due to type erasure, doesn't store the generic type
		data class UntypedIntegrationConfig(val userId: UUID, val config: Document)

		return collection.find<UntypedIntegrationConfig>(
			Filters.eq(GraphNode::type.name, integrationType)
		).map {
			val settings = collection.codecRegistry.decode(clazz, it.config)
			IntegrationConfig(it.userId, settings)
		}
	}

	override suspend fun <S : IntegrationSettings> get(id: ConfigId, integrationType: String, clazz: KClass<S>): S? {
		val settings = collection.find(
			Filters.and(
				Filters.eq(GraphNode::_id.name, id),
				Filters.eq(GraphNode::type.name, integrationType)
			)
		).firstOrNull()?.config ?: return null
		return collection.codecRegistry.decode(clazz, settings)
	}

	override suspend fun <S : IntegrationSettings> create(userID: UUID, integrationType: String, settings: S): ConfigId {
		val node = GraphNode(
			userId = userID,
			type = integrationType,
			config = collection.codecRegistry.encode(settings),
			next = emptyList()
		)
		collection.insertOne(node)
		return node._id
	}
}
