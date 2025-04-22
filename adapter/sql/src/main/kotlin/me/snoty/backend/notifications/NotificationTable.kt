package me.snoty.backend.notifications

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.or
import org.koin.core.annotation.Single

@Single
class NotificationTable(json: Json) : Table("notification") {
	val userId = varchar("user_id", 255)
	// TODO: index on `flowId` and `nodeId`
	val attributes = jsonb<NotificationAttributes>("attributes", json)
	val resolved = bool("resolved").default(false)
	val resolvedAt = timestamp("resolved_at").nullable()

	val count = integer("count").default(1)
	val lastSeenAt = timestamp("last_seen_at").defaultExpression(CurrentTimestamp)

	override val primaryKey = PrimaryKey(userId, attributes, resolved)

	init {
		// prevent inconsistent state
		check {
			((resolved eq false) and (resolvedAt eq null)) or
				((resolved eq true) and (resolvedAt neq null))
		}
	}
}
