package me.snoty.backend.wiring.node

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.slf4j.event.Level
import java.util.*

/**
 * Low-level representation of a flow graph node gotten using `$graphLookup`
 * This class is used to serialize and deserialize flow graphs from the database.
 */
data class MongoNode(
	@BsonId
	val _id: ObjectId = ObjectId(),
	val flowId: ObjectId,
	val userId: UUID,
	val descriptor: NodeDescriptor,
	val logLevel: Level? = null,
	val settings: Document,
	val next: List<ObjectId>?,
)

fun MongoNode.toStandalone(
	settings: NodeSettings,
) = StandaloneNode(
	_id = _id.toHexString(),
	flowId = flowId.toHexString(),
	userId = userId,
	descriptor = descriptor,
	logLevel = logLevel,
	settings = settings,
)

fun MongoNode.toRelational(
	settings: NodeSettings,
): FlowNode = FlowNode(
	_id = _id.toHexString(),
	flowId = flowId.toHexString(),
	userId = userId,
	descriptor = descriptor,
	logLevel = logLevel,
	settings = settings,
	next = next?.map(ObjectId::toHexString) ?: emptyList(),
)
