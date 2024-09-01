package me.snoty.backend.integration.flow

import com.mongodb.client.model.Aggregates.lookup
import com.mongodb.client.model.Aggregates.match
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import me.snoty.backend.database.mongo.aggregate
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.integration.utils.MongoSettingsService
import me.snoty.integration.common.wiring.flow.*
import me.snoty.integration.common.wiring.graph.MongoNode
import me.snoty.integration.common.wiring.graph.toRelational
import org.bson.codecs.pojo.annotations.BsonId
import org.koin.core.annotation.Single
import java.util.*

@Single
class MongoFlowService(
	db: MongoDatabase,
	private val settingsLookup: MongoSettingsService,
) : FlowService {
	private val collection = db.getCollection<MongoWorkflow>(FLOW_COLLECTION_NAME)

	override suspend fun create(userId: UUID, name: String): StandaloneWorkflow {
		val workflow = MongoWorkflow(name = name, userId = userId)
		collection.insertOne(workflow)
		return workflow.toStandalone()
	}

	override fun query(userId: UUID): Flow<StandaloneWorkflow> = collection.aggregate<MongoWorkflow>(
		match(Filters.eq(MongoWorkflow::userId.name, userId))
	).map {
		it.toStandalone()
	}

	override fun getAll(): Flow<StandaloneWorkflow> = collection.aggregate<MongoWorkflow>().map {
		it.toStandalone()
	}

	override suspend fun getStandalone(flowId: NodeId) = collection
		.find(Filters.eq(MongoWorkflow::_id.name, flowId))
		.firstOrNull()
		?.toStandalone()

	override suspend fun getWithNodes(flowId: NodeId) = collection
		.aggregate<MongoWorkflowWithNodes>(
			match(Filters.eq(MongoWorkflow::_id.name, flowId)),
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
			Filters.eq(MongoWorkflow::_id.name, flowId),
			Updates.set(MongoWorkflow::name.name, name)
		)
	}
}

internal data class MongoWorkflow(
	@BsonId
	override val _id: NodeId = NodeId(),
	override val name: String,
	override val userId: UUID,
) : Workflow {
	fun toStandalone() = StandaloneWorkflow(
		_id = _id,
		name = name,
		userId = userId,
	)
}

internal data class MongoWorkflowWithNodes(
	@BsonId
	override val _id: NodeId = NodeId(),
	override val name: String,
	override val userId: UUID,
	val nodes: List<MongoNode>,
) : Workflow {
	fun toRelational(settingsLookup: MongoSettingsService) = WorkflowWithNodes(
		_id = _id,
		name = name,
		userId = userId,
		nodes = nodes.map { it.toRelational(settingsLookup.lookup(it)) },
	)
}
