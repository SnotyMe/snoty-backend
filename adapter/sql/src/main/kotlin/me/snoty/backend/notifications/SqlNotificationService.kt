package me.snoty.backend.notifications

import me.snoty.backend.database.sql.NULL_INSTANT
import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.newSuspendedTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.koin.core.annotation.Single

@Single
class SqlNotificationService(private val db: Database, private val table: NotificationTable) : NotificationService {
	override suspend fun send(userId: String, attributes: NotificationAttributes, title: String, description: String?): Unit = db.newSuspendedTransaction {
		table.upsert(
			onUpdate = {
				it[table.count] = table.count + 1
				it[table.lastSeenAt] = CurrentTimestamp

				it[table.title] = title
				it[table.description] = description
			}
		) {
			it[table.userId] = userId
			it[table.attributes] = attributes
			it[table.resolvedAt] = NULL_INSTANT

			it[table.title] = title
			it[table.description] = description
		}
	}

	override suspend fun resolve(userId: String, attributes: NotificationAttributes): Unit = db.newSuspendedTransaction {
		table.update(where = {
			(table.resolvedAt eq NULL_INSTANT) and (table.userId eq userId) and (table.attributes eq attributes)
		}) {
			it[table.resolvedAt] = CurrentTimestamp
		}
	}

	override fun findByUser(userId: String) = db.flowTransaction {
		table.selectAll()
			.where { table.userId eq userId }
			.orderBy(table.resolvedAt, SortOrder.DESC_NULLS_LAST)
			.map {
				Notification(
					userId = it[table.userId],
					attributes = it[table.attributes],
					resolvedAt = it[table.resolvedAt].takeIf { i -> i != NULL_INSTANT },
					lastSeenAt = it[table.lastSeenAt],
					count = it[table.count],
					title = it[table.title],
					description = it[table.description],
				)
			}
	}

	override suspend fun unresolvedByUser(userId: String): Long = db.newSuspendedTransaction {
		table.selectAll()
			.where { table.userId eq userId and (table.resolvedAt eq NULL_INSTANT) }
			.count()
	}
}
