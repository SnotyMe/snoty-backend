package me.snoty.integration.common.wiring.graph

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.FlowNode
import me.snoty.integration.common.wiring.GenericNode
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import org.slf4j.event.Level
import java.util.*

/**
 * Low-level representation of a flow graph node gotten using `$graphLookup`
 * This class is used to serialize and deserialize flow graphs from the database.
 */
data class MongoNode(
	@BsonId
	override val _id: NodeId = NodeId(),
	override val flowId: NodeId,
	override val userId: UUID,
	override val descriptor: NodeDescriptor,
	override val logLevel: Level? = null,
	val settings: Document,
	val next: List<NodeId>?,
) : GenericNode

fun MongoNode.toStandalone(
	settings: NodeSettings,
) = StandaloneNode(
	_id = _id,
	flowId = flowId,
	userId = userId,
	descriptor = descriptor,
	logLevel = logLevel,
	settings = settings,
)

fun MongoNode.toRelational(
	settings: NodeSettings,
): FlowNode = FlowNode(
	_id = _id,
	flowId = flowId,
	userId = userId,
	descriptor = descriptor,
	logLevel = logLevel,
	settings = settings,
	next = next ?: emptyList()
)
