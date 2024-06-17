package me.snoty.backend.integration.config.flow

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.UUID

data class UserWorkflows(
	val userId: UUID,
	val nodes: List<FlowNode>,
	val connections: List<NodeConnection>,
	@BsonId
	val id: ObjectId = ObjectId()
)
