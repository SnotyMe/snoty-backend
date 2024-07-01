package me.snoty.integration.common.wiring.graph

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.wiring.IFlowNode
import me.snoty.integration.common.wiring.node.NodeDescriptor
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
	override val config: Document,
	val next: List<NodeId>?
) : IFlowNode
