package me.snoty.integration.common.wiring.graph

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.GenericNode
import me.snoty.integration.common.wiring.RelationalFlowNode
import me.snoty.integration.common.wiring.StandaloneNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
import me.snoty.integration.common.wiring.node.NodeSettings
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import java.util.*

/**
 * Low-level representation of a flow graph node gotten using `$graphLookup`
 * This class is used to serialize and deserialize flow graphs from the database.
 */
data class GraphNode(
	@BsonId
	override val _id: NodeId = NodeId(),
	override val userId: UUID,
	override val descriptor: NodeDescriptor,
	val settings: Document,
	val next: List<NodeId>?
) : GenericNode

fun GraphNode.toStandalone(
	settings: NodeSettings
) = StandaloneNode(
	_id = _id,
	userId = userId,
	descriptor = descriptor,
	settings = settings
)

fun GraphNode.toRelational(
	settings: NodeSettings,
	next: List<RelationalFlowNode>
): RelationalFlowNode = RelationalFlowNode(
	_id = _id,
	userId = userId,
	descriptor = descriptor,
	settings = settings,
	next = next
)
