package me.snoty.backend.wiring.node

import me.snoty.backend.database.mongo.toFlowId
import me.snoty.backend.database.mongo.toNodeId
import me.snoty.core.node.FlowNode
import me.snoty.core.node.StandaloneNode
import me.snoty.core.user.UserId
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodePosition
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.slf4j.event.Level
import kotlin.time.Instant

/**
 * Low-level representation of a flow graph node gotten using `$graphLookup`
 * This class is used to serialize and deserialize flow graphs from the database.
 */
data class MongoNode(
	@BsonId
	val _id: ObjectId = ObjectId(),
	val flowId: ObjectId,
	val userId: UserId,
	val descriptor: NodeDescriptor,
	val name: String,
	val logLevel: Level? = null,
	val position: NodePosition,
	val settings: Document,
	val next: List<ObjectId>?,
	val createdAt: Instant,
	val modifiedAt: Instant,
)

fun MongoNode.toStandalone(
	settings: NodeSettings,
) = StandaloneNode(
	id = _id.toNodeId(),
	flowId = flowId.toFlowId(),
	userId = userId,
	descriptor = descriptor,
	name = name,
	logLevel = logLevel,
	position = position,
	createdAt = createdAt,
	modifiedAt = modifiedAt,
	settings = settings,
)

fun MongoNode.toRelational(
	settings: NodeSettings,
): FlowNode = FlowNode(
	id = _id.toNodeId(),
	flowId = flowId.toFlowId(),
	userId = userId,
	descriptor = descriptor,
	name = name,
	logLevel = logLevel,
	position = position,
	settings = settings,
	createdAt = createdAt,
	modifiedAt = modifiedAt,
	next = next?.map(ObjectId::toNodeId) ?: emptyList(),
)
