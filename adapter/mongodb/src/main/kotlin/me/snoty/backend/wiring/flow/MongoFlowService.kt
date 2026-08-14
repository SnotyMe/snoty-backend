package me.snoty.backend.wiring.flow

import com.mongodb.client.model.Aggregates.lookup
import com.mongodb.client.model.Aggregates.match
import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.snoty.backend.database.mongo.aggregate
import me.snoty.backend.database.mongo.deserializeOrInvalid
import me.snoty.backend.database.mongo.objectId
import me.snoty.backend.database.mongo.toFlowId
import me.snoty.backend.scheduling.FlowScheduler
import me.snoty.backend.wiring.node.MongoNode
import me.snoty.backend.wiring.node.NodeSettingsDeserializationService
import me.snoty.backend.wiring.node.toRelational
import me.snoty.core.FlowId
import me.snoty.core.UserId
import me.snoty.integration.common.wiring.flow.*
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.koin.core.annotation.Single

@Single
class MongoFlowService(
	db: MongoDatabase,
	private val flowScheduler: FlowScheduler,
	private val settingsDeserializationService: NodeSettingsDeserializationService,
) : FlowService {
	private val collection = db.getCollection<MongoWorkflow>(FLOW_COLLECTION_NAME)

	override suspend fun create(userId: UserId, name: String, settings: WorkflowSettings): StandaloneWorkflow {
		val mongoWorkflow = MongoWorkflow(name = name, userId = userId, settings = settings)
		collection.insertOne(mongoWorkflow)
		val workflow = mongoWorkflow.toStandalone()
		flowScheduler.schedule(workflow)
		return workflow
	}

	override fun query(userId: UserId): Flow<StandaloneWorkflow> = collection.find(
		Filters.eq(MongoWorkflow::userId.name, userId)
	).map {
		it.toStandalone()
	}

	override fun getAll(): Flow<StandaloneWorkflow> = collection.find().map {
		it.toStandalone()
	}

	override suspend fun getStandalone(userId: UserId, flowId: FlowId) = collection
		.find(
			Filters.and(
				Filters.eq(MongoWorkflow::userId.name, userId),
				Filters.eq(MongoWorkflow::_id.name, flowId.objectId),
			)
		)
		.firstOrNull()
		?.toStandalone()

	override suspend fun getWithNodes(userId: UserId?, flowId: FlowId) = collection
		.aggregate<MongoWorkflowWithNodes>(
			match(
				Filters.and(
					if (userId != null) Filters.eq(MongoWorkflow::userId.name, userId) else Filters.empty(),
					Filters.eq(MongoWorkflow::_id.name, flowId.objectId)
				)
			),
			lookup(
				/* from = */ NODE_COLLECTION_NAME,
				/* localField = */ MongoWorkflow::_id.name,
				/* foreignField = */ MongoNode::flowId.name,
				/* as = */ MongoWorkflowWithNodes::nodes.name,
			),
		)
		.firstOrNull()
		?.toRelational(settingsDeserializationService)

	override suspend fun rename(flow: Workflow, name: String) {
		collection.updateOne(
			Filters.eq(MongoWorkflow::_id.name, flow.objectId),
			Updates.set(MongoWorkflow::name.name, name)
		)
	}

	override suspend fun updateSettings(flow: Workflow, settings: WorkflowSettings) {
		val workflow = collection.findOneAndUpdate(
			Filters.eq(MongoWorkflow::_id.name, flow.objectId),
			Updates.set(MongoWorkflow::settings.name, settings),
			FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
		)!!.toStandalone()

		flowScheduler.reschedule(workflow)
	}

	override suspend fun delete(flow: Workflow) {
		collection.deleteOne(Filters.eq(MongoWorkflow::_id.name, flow.objectId))
	}
}

data class MongoWorkflow(
	@BsonId
	val _id: ObjectId = ObjectId(),
	val name: String,
	val userId: UserId,
	val settings: WorkflowSettings?,
) {
	fun toStandalone() = StandaloneWorkflow(
		id = _id.toFlowId(),
		name = name,
		userId = userId,
		settings = settings ?: WorkflowSettings(),
	)
}

data class MongoWorkflowWithNodes(
	@BsonId
	val _id: ObjectId = ObjectId(),
	val name: String,
	val userId: UserId,
	val settings: WorkflowSettings?,
	val nodes: List<MongoNode>,
) {
	fun toRelational(settingsLookup: NodeSettingsDeserializationService) = WorkflowWithNodes(
		id = _id.toFlowId(),
		name = name,
		userId = userId,
		settings = settings ?: WorkflowSettings(),
		nodes = nodes.map {
			val settings = settingsLookup.deserializeOrInvalid(it)
			it.toRelational(settings)
		},
	)
}
