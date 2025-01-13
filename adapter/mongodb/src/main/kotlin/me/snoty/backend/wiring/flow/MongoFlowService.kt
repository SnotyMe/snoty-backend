package me.snoty.backend.wiring.flow

import com.mongodb.client.model.Aggregates.lookup
import com.mongodb.client.model.Aggregates.match
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.snoty.backend.database.mongo.aggregate
import me.snoty.backend.database.mongo.deserializeOrInvalid
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.wiring.node.NodeSettingsSerializationService
import me.snoty.backend.wiring.node.deserializeOrInvalid
import me.snoty.integration.common.wiring.flow.*
import me.snoty.backend.wiring.node.MongoNode
import me.snoty.backend.wiring.node.toRelational
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.koin.core.annotation.Single
import java.util.*

@Single
class MongoFlowService(
	db: MongoDatabase,
	private val flowScheduler: FlowScheduler,
	private val settingsLookup: NodeSettingsSerializationService,
) : FlowService {
	private val collection = db.getCollection<MongoWorkflow>(FLOW_COLLECTION_NAME)

	override suspend fun create(userId: UUID, name: String): StandaloneWorkflow {
		val mongoWorkflow = MongoWorkflow(name = name, userId = userId)
		collection.insertOne(mongoWorkflow)
		var workflow = mongoWorkflow.toStandalone()
		flowScheduler.schedule(workflow)
		return workflow
	}

	override fun query(userId: UUID): Flow<StandaloneWorkflow> = collection.find(
		Filters.eq(MongoWorkflow::userId.name, userId)
	).map {
		it.toStandalone()
	}

	override fun getAll(): Flow<StandaloneWorkflow> = collection.find().map {
		it.toStandalone()
	}

	override suspend fun getStandalone(flowId: NodeId) = collection
		.find(Filters.eq(MongoWorkflow::_id.name, ObjectId(flowId)))
		.firstOrNull()
		?.toStandalone()

	override suspend fun getWithNodes(flowId: NodeId) = collection
		.aggregate<MongoWorkflowWithNodes>(
			match(Filters.eq(MongoWorkflow::_id.name, ObjectId(flowId))),
			lookup(
				/* from = */ NODE_COLLECTION_NAME,
				/* localField = */ Workflow::_id.name,
				/* foreignField = */ MongoNode::flowId.name,
				/* as = */ MongoWorkflowWithNodes::nodes.name,
			),
		)
		.firstOrNull()
		?.toRelational(settingsLookup)

	override suspend fun rename(flowId: NodeId, name: String) {
		collection.updateOne(
			Filters.eq(MongoWorkflow::_id.name, ObjectId(flowId)),
			Updates.set(MongoWorkflow::name.name, name)
		)
	}

	override suspend fun delete(flowId: NodeId) {
		collection.deleteOne(Filters.eq(MongoWorkflow::_id.name, ObjectId(flowId)))
	}
}

data class MongoWorkflow(
	@BsonId
	val _id: ObjectId = ObjectId(),
	val name: String,
	val userId: UUID,
) {
	fun toStandalone() = StandaloneWorkflow(
		_id = _id.toHexString(),
		name = name,
		userId = userId,
	)
}

data class MongoWorkflowWithNodes(
	@BsonId
	val _id: ObjectId = ObjectId(),
	val name: String,
	val userId: UUID,
	val nodes: List<MongoNode>,
) {
	fun toRelational(settingsLookup: NodeSettingsSerializationService) = WorkflowWithNodes(
		_id = _id.toHexString(),
		name = name,
		userId = userId,
		nodes = nodes.map {
			val settings = settingsLookup.deserializeOrInvalid(it)
			it.toRelational(settings)
		},
	)
}
