package me.snoty.integration.common.diff.state

import com.mongodb.kotlin.client.coroutine.MongoCollection
import me.snoty.integration.common.InstanceId
import me.snoty.integration.common.diff.Fields
import org.bson.codecs.pojo.annotations.BsonId
import java.util.*

data class EntityState(
	val id: String,
	val type: String,
	val state: Fields,
	val checksum: Long
)

data class UserEntityStates(
	@BsonId
	val userId: UUID,
	val entities: Map<InstanceId, Set<EntityState>>
)

typealias EntityStateCollection = MongoCollection<UserEntityStates>
