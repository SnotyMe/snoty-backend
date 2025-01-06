package me.snoty.integration.common.diff.state

import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.diff.checksum
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId

data class EntityState(
	val id: String,
	val state: Document,
	val checksum: Long,
) {
	constructor(id: String, state: Document) : this(id, state, state.checksum())
}

data class NodeEntityStates(
	@BsonId
	val nodeId: NodeId,
	val entities: Set<EntityState>
)
