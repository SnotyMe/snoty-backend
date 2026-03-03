package me.snoty.backend.notifications

import kotlinx.serialization.json.Json
import me.snoty.backend.database.sql.utils.UuidTable
import me.snoty.backend.database.sql.utils.userId
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import org.koin.core.annotation.Single

@Single(binds = [Table::class])
class NotificationTable(json: Json) : UuidTable("notification") {
	val userId = userId("user_id")
	val attributes = jsonb<NotificationAttributes>("attributes", json)
	/**
	 * Set to null when the notification is resolved. Due to the fact that null != null (lol), it'll allow multiple resolved notifications.
	 */
	val open = bool("open").nullable().default(true)

	val resolvedAt = timestamp("resolved_at").nullable().default(null)

	val count = integer("count").default(1)
	val lastSeenAt = timestamp("last_seen_at").defaultExpression(CurrentTimestamp)

	val title = text("title")
	val description = text("description").nullable()

	init {
		uniqueIndex(userId, attributes, open)

		check("notification_open_resolved_at_consistent") {
			((open eq true) and (resolvedAt eq null)) or
				((open eq null) and (resolvedAt neq null))
		}
	}
}
