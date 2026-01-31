package me.snoty.backend.notifications

import me.snoty.backend.database.sql.flowTransaction
import me.snoty.backend.database.sql.suspendTransaction
import me.snoty.backend.utils.toUuid
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.*
import org.koin.core.annotation.Single

@Single
class SqlNotificationService(private val db: Database, private val table: NotificationTable) : NotificationService {
	override suspend fun send(userId: String, attributes: NotificationAttributes, title: String, description: String?): Unit = db.suspendTransaction {
		table.upsert(
			table.userId, table.attributes, table.open,
			onUpdate = {
				it[table.count] = table.count + 1
				it[table.lastSeenAt] = CurrentTimestamp

				it[table.title] = title
				it[table.description] = description
			}
		) {
			it[table.userId] = userId
			it[table.attributes] = attributes
			it[table.open] = true

			it[table.title] = title
			it[table.description] = description
		}
	}

	override suspend fun resolve(userId: String, attributes: NotificationAttributes): Unit = db.suspendTransaction {
		table.update(where = {
			(table.open eq true) and (table.userId eq userId) and (table.attributes eq attributes)
		}) {
			it[table.open] = null // null, not false, to allow multiple resolved notifications (null != null)
			it[table.resolvedAt] = CurrentTimestamp
		}
	}

	override fun findByUser(userId: String) = db.flowTransaction {
		table.selectAll()
			.where { table.userId eq userId }
			.orderBy(table.lastSeenAt, SortOrder.DESC)
			.map {
				Notification(
					id = it[table.id].toString(),
					userId = it[table.userId],
					attributes = it[table.attributes],
					resolvedAt = it[table.resolvedAt],
					lastSeenAt = it[table.lastSeenAt],
					count = it[table.count],
					title = it[table.title],
					description = it[table.description],
				)
			}
	}

	override suspend fun unresolvedByUser(userId: String): Long = db.suspendTransaction {
		table.selectAll()
			.where { (table.userId eq userId) and (table.open eq true) }
			.count()
	}

	override suspend fun delete(userId: String, id: String) = db.suspendTransaction {
		table.deleteWhere { (table.userId eq userId) and (table.id eq id.toUuid()) } > 0
	}
}
