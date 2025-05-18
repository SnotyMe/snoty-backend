package me.snoty.backend.notifications

import kotlinx.serialization.json.Json
import me.snoty.backend.database.sql.utils.UuidTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.or
import org.koin.core.annotation.Single

@Single(binds = [Table::class])
class NotificationTable(json: Json) : UuidTable("notification") {
	val userId = varchar("user_id", 255)
	val attributes = jsonb<NotificationAttributes>("attributes", json)
	/**
	 * Set to [null] when the notification is resolved. Due to the fact that null != null (lol), it'll allow multiple resolved notifications.
	 */
	val open = bool("open").nullable().default(true)

	val resolvedAt = timestamp("resolved_at").nullable().default(null)

	val count = integer("count").default(1)
	val lastSeenAt = timestamp("last_seen_at").defaultExpression(CurrentTimestamp)

	val title = varchar("title", 255)
	val description = varchar("description", 255).nullable()

	init {
		uniqueIndex(userId, attributes, open)

		check("inconsistent_resolved_state") {
			((open eq true) and (resolvedAt eq null)) or
				((open eq null) and (resolvedAt neq null))
		}
	}
}
