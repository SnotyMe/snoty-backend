package me.snoty.backend.notification

import me.snoty.backend.notifications.Notification
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import kotlin.time.Instant

data class MongoNotification(
	@BsonId
	val _id: ObjectId,
	val userId: String,
	val attributes: Document,
	val resolvedAt: Instant? = null,
	val lastSeenAt: Instant,
	val count: Int,
	val title: String,
	val description: String? = null,
)

fun MongoNotification.toNotification() = Notification(
	id = _id.toHexString(),
	userId = userId,
	attributes = attributes.entries.associate { it.key to it.value.toString() },
	resolvedAt = resolvedAt,
	lastSeenAt = lastSeenAt,
	count = count,
	title = title,
	description = description
)
