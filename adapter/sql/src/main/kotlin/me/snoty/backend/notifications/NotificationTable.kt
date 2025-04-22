package me.snoty.backend.notifications

import kotlinx.serialization.json.Json
import me.snoty.backend.database.sql.NULL_INSTANT
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.koin.core.annotation.Single

@Single
class NotificationTable(json: Json) : Table("notification") {
	val userId = varchar("user_id", 255)
	val attributes = jsonb<NotificationAttributes>("attributes", json)
	val resolvedAt = timestamp("resolved_at").default(NULL_INSTANT)

	val count = integer("count").default(1)
	val lastSeenAt = timestamp("last_seen_at").defaultExpression(CurrentTimestamp)

	val title = varchar("title", 255)
	val description = varchar("description", 255).nullable()

	override val primaryKey = PrimaryKey(userId, attributes, resolvedAt)
}
