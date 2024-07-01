package me.snoty.integration.common.diff.state

import com.mongodb.kotlin.client.coroutine.MongoCollection
import me.snoty.backend.integration.config.flow.NodeId
import me.snoty.integration.common.diff.Fields
import org.bson.codecs.pojo.annotations.BsonId

data class EntityState(
	val id: String,
	val type: String,
	val state: Fields,
	val checksum: Long
)

data class NodeEntityStates(
	@BsonId
	val nodeId: NodeId,
	val entities: Set<EntityState>
)

typealias EntityStateCollection = MongoCollection<NodeEntityStates>
