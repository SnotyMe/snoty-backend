package me.snoty.backend.integration.flow.logging

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import me.snoty.backend.database.mongo.upsertOne
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.flow.FLOW_COLLECTION_NAME
import me.snoty.integration.common.wiring.flow.NodeLogEntry
import org.bson.codecs.pojo.annotations.BsonId
import org.koin.core.annotation.Single

interface NodeLogService {
	suspend fun record(rootNode: NodeId, entry: NodeLogEntry)

	suspend fun retrieve(rootNode: NodeId): List<NodeLogEntry>
}

internal data class NodeLogs(
	@BsonId
	val _id: NodeId,
	val logs: List<NodeLogEntry>
)

@Single
class MongoNodeLogService(mongoDB: MongoDatabase) : NodeLogService {
	private val collection = mongoDB.getCollection<NodeLogs>("$FLOW_COLLECTION_NAME.log")

	override suspend fun record(rootNode: NodeId, entry: NodeLogEntry) {
		collection.upsertOne(
			Filters.eq(NodeLogs::_id.name, rootNode),
			Updates.push(NodeLogs::logs.name, entry)
		)
	}

	override suspend fun retrieve(rootNode: NodeId): List<NodeLogEntry> =
		collection.find(Filters.eq(NodeLogs::_id.name, rootNode))
			.firstOrNull()
			?.logs
			?: emptyList()
}
