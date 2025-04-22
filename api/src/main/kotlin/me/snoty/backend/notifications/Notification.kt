package me.snoty.backend.notifications

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

typealias NotificationAttributes = Map<String, String>

@Serializable
data class Notification(
	val userId: String,
	val attributes: NotificationAttributes,
	val resolvedAt: Instant? = null,
	val lastSeenAt: Instant,
	val count: Int,
)
