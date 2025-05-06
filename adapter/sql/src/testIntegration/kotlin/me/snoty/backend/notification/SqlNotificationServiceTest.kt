package me.snoty.backend.notification

import kotlinx.datetime.Clock
import me.snoty.backend.database.sql.PostgresTest
import me.snoty.backend.notifications.NotificationServiceSpec
import me.snoty.backend.notifications.NotificationTable
import me.snoty.backend.notifications.SqlNotificationService
import me.snoty.integration.common.snotyJson
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SqlNotificationServiceTest : NotificationServiceSpec() {
	private val notificationTable = NotificationTable(snotyJson {})
	val db = PostgresTest.getPostgresDatabase {
		SchemaUtils.create(notificationTable)
	}

	override val notificationService = SqlNotificationService(
		db = db,
		table = notificationTable,
	)

	@Test
	fun inconsistentResolvedState() = test {
		val attributes = attributes("key" to "value")
		notificationService.send(attributes)

		assertThrows<IllegalStateException> {
			notificationTable.update(where = {
				notificationTable.userId eq userId and (notificationTable.attributes eq attributes)
			}) {
				it[notificationTable.open] = true
				it[notificationTable.resolvedAt] = null
			}
		}

		assertThrows<IllegalStateException> {
			notificationTable.update(where = {
				notificationTable.userId eq userId and (notificationTable.attributes eq attributes)
			}) {
				it[notificationTable.open] = false
				it[notificationTable.resolvedAt] = Clock.System.now()
			}
		}
	}

	@Test
	fun openFalseIsIllegal() = test {
		val attributes = attributes("key" to "value")
		notificationService.send(attributes)

		assertThrows<IllegalStateException> {
			notificationTable.update(where = {
				notificationTable.userId eq userId and (notificationTable.attributes eq attributes)
			}) {
				it[notificationTable.open] = false
			}
		}
	}
}
