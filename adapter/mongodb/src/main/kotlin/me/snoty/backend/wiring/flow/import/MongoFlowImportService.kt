package me.snoty.backend.wiring.flow.import

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOneModel
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.backend.wiring.node.MongoNode
import me.snoty.integration.common.wiring.flow.FlowService
import me.snoty.integration.common.wiring.flow.NODE_COLLECTION_NAME
import me.snoty.integration.common.wiring.flow.WorkflowSettings
import org.bson.types.ObjectId
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class MongoFlowImportService(
	private val flowService: FlowService,
	db: MongoDatabase,
) : FlowImportService {
	private val nodeCollection = db.getCollection<MongoNode>(NODE_COLLECTION_NAME)

	override suspend fun import(userId: Uuid, flow: ImportFlow): NodeId {
		val createdFlow = flowService.create(userId, flow.name, WorkflowSettings())
		val createdFlowId = ObjectId(createdFlow._id)

		val nodesToInsert = flow.nodes.map {
			MongoNode(
				flowId = createdFlowId,
				userId = userId,
				descriptor = it.descriptor,
				settings = it.settings,
				next = emptyList()
			)
		}
		val createdNodes = nodeCollection.insertMany(nodesToInsert)
			.insertedIds
			.map { flow.nodes[it.key] to it.value.asObjectId().value }
			.toMap()

		nodeCollection.bulkWrite(
			createdNodes.map { (imported, mongoId) ->
				UpdateOneModel(
					Filters.eq(MongoNode::_id.name, mongoId),
					Updates.set(
						MongoNode::next.name,
						imported.next.map {
							createdNodes.filterKeys { key -> key.id == it }
								.values
								.first()
						}
					)
				)
			}
		)

		return createdFlow._id
	}
}
