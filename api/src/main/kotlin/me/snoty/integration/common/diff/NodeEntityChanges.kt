package me.snoty.integration.common.diff

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.snoty.backend.integration.config.flow.NodeId
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class EntityDescriptor(
	val nodeId: NodeId,
	val entityId: String,
)

data class UserEntityChanges(
	val change: DiffResult,
	val time: Instant = Clock.System.now(),
	@BsonId
	val changeId: ObjectId = ObjectId()
)
