package me.snoty.backend.integration.flow.model.graph

import me.snoty.backend.integration.config.flow.NodeId
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import java.util.UUID

/**
 * Low-level representation of a flow graph node gotten using `$graphLookup`
 * This class is used to serialize and deserialize flow graphs from the database.
 */
data class GraphNode(
	@BsonId
	val _id: NodeId = NodeId(),
	val userId: UUID,
	val type: String,
	val config: Document,
	val next: List<NodeId>?
)
