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

interface FlowLogService {
	suspend fun record(flowId: NodeId, entry: NodeLogEntry)

	suspend fun retrieve(flowId: NodeId): List<NodeLogEntry>
}

internal data class FlowLogs(
	@BsonId
	val _id: NodeId,
	val logs: List<NodeLogEntry>
)

@Single
class MongoFlowLogService(mongoDB: MongoDatabase) : FlowLogService {
	private val collection = mongoDB.getCollection<FlowLogs>("$FLOW_COLLECTION_NAME.log")

	override suspend fun record(flowId: NodeId, entry: NodeLogEntry) {
		collection.upsertOne(
			Filters.eq(FlowLogs::_id.name, flowId),
			Updates.push(FlowLogs::logs.name, entry)
		)
	}

	override suspend fun retrieve(flowId: NodeId): List<NodeLogEntry> =
		collection.find(Filters.eq(FlowLogs::_id.name, flowId))
			.firstOrNull()
			?.logs
			?: emptyList()
}
