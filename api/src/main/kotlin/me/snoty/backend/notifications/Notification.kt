package me.snoty.backend.notifications

import kotlinx.serialization.Serializable
import me.snoty.backend.observability.FLOW_ID
import me.snoty.backend.observability.NODE_ID
import kotlin.time.Instant

typealias NotificationAttributes = Map<String, String>

const val NOTIFICATION_TYPE = "notificationType"

fun NotificationAttributes(
	type: String,
	flowId: String,
	nodeId: String,
	vararg extra: Pair<String, String>,
) = NotificationAttributes(
	type,
	flowId,
	NODE_ID.key to nodeId,
	*extra,
)

fun NotificationAttributes(
	type: String,
	flowId: String,
	vararg extra: Pair<String, String>,
): NotificationAttributes = NotificationAttributes(
	type,
	FLOW_ID.key to flowId,
	*extra
)

fun NotificationAttributes(
	type: String,
	vararg extra: Pair<String, String>,
): NotificationAttributes = mapOf(
	NOTIFICATION_TYPE to type,
	*extra,
)

@Serializable
data class Notification(
	val id: String,
	val userId: String,
	val attributes: NotificationAttributes,
	val resolvedAt: Instant? = null,
	val lastSeenAt: Instant,
	val count: Int,

	val title: String,
	val description: String? = null,
)
