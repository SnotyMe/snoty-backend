package me.snoty.integration.common.diff

import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.snoty.integration.common.InstanceId
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.*

data class Descriptor(
	val instanceId: InstanceId,
	val entityType: String,
	val entityId: String,
	val userId: UUID
)

data class UserEntityChanges(
	val descriptor: Descriptor,
	val change: DiffResult,
	val time: Instant = Clock.System.now(),
	@BsonId
	val changeId: ObjectId = ObjectId()
)


typealias EntityChangeCollection = MongoCollection<UserEntityChanges>
